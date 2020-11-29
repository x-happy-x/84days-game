package ru.happy.game.adventuredog.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import ru.happy.game.adventuredog.MainGDX;

public class NetTask {
    // Параметры
    public static final String site = "http://api.happy-x.ru/DogGame/";
    public int cur_progress, max_progress;
    public boolean hardRun, killed;
    public String result;
    public int loadingFLSize, loadingFASize;
    private NetListener listener;
    private boolean isFile;
    private Thread task;

    // Конструкторы
    public NetTask() {
        this(null);
    }

    public NetTask(NetListener listener) {
        this.listener = listener;
        cur_progress = max_progress = 0;
        hardRun = false;
        result = "";
    }

    // Загрузить файл
    public void loadFile(String url, String path, boolean resume) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_GET_(site + url, resume, new File(path))));
            task.start();
            hardRun = false;
        }
    }

    // Загрузить несколько файлов
    public void loadFiles(String url, String path, boolean resume, boolean stopIfError, Object... files) {
        if (!isAlive()) {
            task = new Thread(() -> {
                boolean success;
                loadingFASize = 0;
                for (Object f : files) {
                    if (killed) return;
                    loadingFASize += getFileSize(site + url + ((File) f).getName());
                }
                for (Object f : files) {
                    if (killed) return;
                    success = _GET_(site + url + ((File) f).getName(), resume, new File(((File) f).getParentFile().getAbsolutePath() + "/" + path, ((File) f).getName()));
                    onPostExecute(success);
                    if (stopIfError && !success) break;
                }
                loadingFASize = 0;
            });
            task.start();
            hardRun = false;
        }
    }

    public void loadFiles(String url, boolean resume, boolean stopIfError, Object... files) {
        loadFiles(url, null, resume, stopIfError, files);
    }

    public void loadFiles(String url, boolean stopIfError, Object... files) {
        loadFiles(url, true, stopIfError, files);
    }

    public void loadFiles(String url, String path, Object... files) {
        loadFiles(url, path, true, false, files);
    }

    public void loadFiles(String url, Object... files) {
        loadFiles(url, false, files);
    }

    // Выгрузить файл
    public void uploadFile(String user, String pass, File... file) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_POST_(site + "File/upload.php?user=" + user + "&pass=" + pass, null, file)));
            task.start();
            hardRun = false;
        }
    }

    // Выполнить GET-запрос (новый поток)
    public void GET(String url, String... params) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_GET_(site + url, false, null, params)));
            task.start();
            hardRun = false;
        }
    }

    // Выполнить GET-запрос (основной поток)
    public boolean SYNC_GET(String url, String... params) {
        return _GET_(site + url, false, null, params);
    }

    public boolean SYNC_GET(String url, boolean resume, File file) {
        return _GET_(url, resume, file);
    }

    // Выполнить POST-запрос (новый поток)
    public void POST(String url, String[] params, File... file) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_POST_(site + url, params, file)));
            task.start();
            hardRun = false;
        }
    }

    // GET запрос
    private boolean _GET_(String _url_, boolean resume, File file, String... params) {
        isFile = file != null;
        try {
            if (killed) return false;
            HttpUrl.Builder httpBuilder = HttpUrl.parse(_url_).newBuilder();
            if (params != null) {
                for (int i = 0; i + 1 < params.length; i++)
                    httpBuilder.addQueryParameter(params[i], params[++i]);
            }
            Request.Builder requestBuilder = new Request.Builder().url(httpBuilder.build());
            MainGDX.write(requestBuilder.build().toString());
            int downloadedSize = 0, fileSize = getFileSize(requestBuilder.build().url().url());
            if (isFile) {
                if (!file.exists() || fileSize < file.length() && file.delete()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            }
            if (killed) return false;
            if (resume && isFile) {
                downloadedSize = (int) file.length();
                if (fileSize > downloadedSize)
                    requestBuilder.addHeader("Range", "bytes=" + downloadedSize + "-");
                else if (fileSize > 0) {
                    if (loadingFASize == 0)
                        onProgressUpdate(downloadedSize, downloadedSize);
                    else {
                        loadingFLSize += downloadedSize;
                        onProgressUpdate(loadingFLSize, loadingFASize);
                    }
                    result = file.getAbsolutePath();
                    return true;
                }
            }
            Request request = requestBuilder.build();
            OkHttpClient client = new OkHttpClient().newBuilder().addNetworkInterceptor(chain -> {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body())).build();
            }).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                int count;
                byte[] buffer = new byte[1024];
                if (file != null) {
                    FileOutputStream fos = new FileOutputStream(file, resume && downloadedSize > 0);
                    while ((count = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                    is.close();
                    fos.close();
                    response.close();
                    result = file.getAbsolutePath();
                } else {
                    result = response.body() != null ? response.body().string() : "Пусто";
                    response.close();
                    return !killed;
                }
                return true;
            } else {
                throw new IOException("Fail GET-query: " + response);
            }
        } catch (IOException e) {
            result = e.getLocalizedMessage();
        }
        return false;
    }

    // POST запрос
    private boolean _POST_(String url, String[] params, File... files) {
        isFile = false;
        OkHttpClient httpClient = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (files != null)
            for (File file : files)
                requestBody.addFormDataPart("file[]", file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), file));
        if (params != null)
            for (int i = 0; i < params.length; )
                requestBody.addFormDataPart(params[i++], params[i++]);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody.build())
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                result = response.body() != null ? response.body().string() : "Пусто";
                response.close();
                return true;
            }
            result = "Что-то пошло не так" + response.message() + response.toString();
        } catch (IOException e) {
            result = "Ой, возникли проблемы при попытке подключиться";
        }
        return false;
    }

    // Метод при выполнения запроса
    private void onProgressUpdate(int size, int all) {
        cur_progress = size;
        max_progress = all;
        if (listener != null) listener.onProgressUpdate(size, all);
    }

    // Метод после выполнения запроса
    private void onPostExecute(boolean result) {
        MainGDX.write("Request{success=\"" + result + "\", result=\"" + (isFile ? "FILE: " + this.result : this.result) + "\"}");
        if (killed) return;
        if (listener != null) {
            if (!result) {
                if (isFile) listener.onDownloadFailure(new File(this.result));
                listener.onDownloadFailure(this.result);
            } else {
                if (isFile) listener.onDownloadComplete(new File(this.result));
                listener.onDownloadComplete(this.result);
            }
        }
    }

    // Загружается
    public boolean isAlive() {
        return !hardRun && (task != null && task.isAlive());
    }

    // Принудительная остановка
    public void kill() {
        killed = true;
    }

    // Установить новый слушатель
    public void setListener(NetListener listener) {
        this.listener = listener;
    }

    // Парсинг ссылки
    private String parseURL(String url, String... params) {
        String newUrl = url;
        for (int i = 0; i < params.length; i++) {
            try {
                newUrl = newUrl.replace("{" + i + "}", URLEncoder.encode(params[i], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                newUrl = newUrl.replace("{" + i + "}", params[i]);
            }
        }
        return newUrl;
    }

    private URL toURL(String url, String... params) throws MalformedURLException {
        return new URL(parseURL(url, params));
    }

    // Размер файла
    private int getFileSize(URL url) {
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            return conn.getContentLength();
        } catch (IOException ignored) {
        }
        return -1;
    }

    public int getFileSize(String url, String... params) {
        try {
            URL url_ = toURL(url, params);
            return getFileSize(url_);
        } catch (MalformedURLException ignored) {
        }
        return -1;
    }

    // Слушатель для обработки результатов запросов
    public static class NetListener {
        public void onDownloadComplete(String msg) {

        }

        public void onDownloadComplete(File result) {

        }

        public void onProgressUpdate(int progress) {

        }

        public void onProgressUpdate(int loaded, int all) {
            if (all > 0) onProgressUpdate(loaded * 100 / all);
            else onProgressUpdate(100);
        }

        public void onDownloadFailure(String msg) {
        }

        public void onDownloadFailure(File result) {
        }
    }

    // Объект для получения прогресса загрузки
    private class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private BufferedSource bufferedSource;
        private int totalBytesRead = 0;

        ProgressResponseBody(ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (loadingFASize == 0)
                        onProgressUpdate(totalBytesRead, (int) responseBody.contentLength());
                    else {
                        loadingFLSize += bytesRead;
                        onProgressUpdate(loadingFLSize, loadingFASize);
                    }
                    return bytesRead;
                }
            };
        }
    }
}