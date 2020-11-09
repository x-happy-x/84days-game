package ru.happy.game.adventuredog.Tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetTask {

    public static final String site = "http://api.happy-x.ru/DogGame/";
    private NetListener listener;
    public String result;
    public boolean hardRun;
    Thread task;

    // Конструкторы
    public NetTask(NetListener listener) {
        this.listener = listener;
        hardRun = false;
        result = "";
    }

    public NetTask() {
        this(null);
    }

    // Загрузить файл
    public void loadFile(String url, String path) {
        if (!isAlive()) {
            task = new Thread(() -> {
                onPostExecute(_GET(site + url, new File(path)));
            });
            task.start();
            hardRun = false;
        }
    }

    // Загрузить файл
    public void loadFiles(String url, Object... files) {
        if (!isAlive()) {
            task = new Thread(() -> {
                for (Object f: files)
                    onPostExecute(_GET(site + url+((File)f).getName(), (File)f));
            });
            task.start();
            hardRun = false;
        }
    }

    // Выгрузить файл
    public void uploadFile(String user, String pass, File... file) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_POST_(site + "File/upload.php?user=" + user + "&pass=" + pass, null, file)));
            task.start();
            hardRun = false;
        }
    }

    // Выполнить запрос
    public void GET(String url, String... params) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_GET_(site + url, params)));
            task.start();
            hardRun = false;
        }
    }

    public boolean AsyncGET(String url, String... params) {
        return _GET_(site + url, params);
    }

    public void POST(String url, String[] params, File... file) {
        if (!isAlive()) {
            task = new Thread(() -> onPostExecute(_POST_(site + url, params, file)));
            task.start();
            hardRun = false;
        }
    }

    // Загружается
    public boolean isAlive() {
        return !hardRun && (task != null && task.isAlive());
    }

    // Установить новый слушатель
    public void setListener(NetListener listener) {
        this.listener = listener;
    }

    public boolean _GET(String _url_, File path, String... params) {
        // Добавление параметров в ссылку
        String sUrl = _url_;
        for (int i = 0; i < params.length; i++) {
            try {
                sUrl = sUrl.replace("{" + i + "}", URLEncoder.encode(params[i], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sUrl = sUrl.replace("{" + i + "}", params[i]);
            }
        }
        // Создание папок для выходного файла, при их отсутствии
        if (path != null && !path.getParentFile().exists()) {
            path.getParentFile().mkdirs();
        }
        try {
            // Создание ссылки
            URL url = new URL(sUrl);
            // Если возможно получить размер файла
            int fileLength = 0;
            if (path != null) {
                URLConnection conn = url.openConnection();
                conn.connect();
                fileLength = conn.getContentLength();
            }
            // Создание входного буфера
            InputStream is = new BufferedInputStream(url.openStream());
            OutputStream os = null;
            ByteArrayOutputStream outputStream = null;
            result = "";
            // Создание выходного буфера
            if (path != null) os = new FileOutputStream(path);
            else outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            long totalDownloaded = 0;
            int count;
            // Выполнение запроса
            while ((count = is.read(buffer)) != -1) {
                totalDownloaded += count;
                if (path != null) {
                    onProgressUpdate((int) (totalDownloaded * 100 / fileLength));
                    os.write(buffer, 0, count);
                } else outputStream.write(buffer, 0, count);
            }
            // Сохранение результата и закрытие соединений
            if (path != null) {
                result = path.getName();
                os.flush();
                os.close();
            } else {
                result = outputStream.toString();
                outputStream.close();
            }
            is.close();
            return true;
        } catch (MalformedURLException e) {
            result = "{\"success\":0,\"message\":\"" + "Не правильная ссылка_Если вы видите эту ошибку свяжитесь с разработчиками" + "\"}";
        } catch (IOException e) {
            result = "{\"success\":0,\"message\":\"" + "Нет подключения к интернету..._" + e.toString() + "\"}";
        }
        return false;
    }

    private boolean _GET_(String _url_, String... params) {
        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder httpBuilder = HttpUrl.parse(_url_).newBuilder();
        if (params != null)
            for (int i = 0; i < params.length; i++)
                httpBuilder.addQueryParameter(params[i], params[++i]);
        Request request = new Request.Builder().url(httpBuilder.build()).build();
        System.out.println(request.toString());
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            result = response.body().string();
            return true;
        } catch (IOException e) {
            result = "Ой оооой, возникли проблемы при попытке соединения";
        }
        return false;
    }

    private boolean _POST_(String url, String[] params, File... files) {
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
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body() != null ? response.body().string() : "Пусто";
                response.close();
                return true;
            }
            result = "Что-то пошло не так" + response.message() + response.toString();
        } catch (IOException e) {
            result = "Ой, возникли проблемы при попытке подключиться";
        }
        response.close();
        return false;
    }

    // Метод после обновления выполнения запроса
    private void onProgressUpdate(Integer... values) {
        if (listener != null) listener.onProgressUpdate(values[0]);
    }

    // Метод после выполнения запроса
    private void onPostExecute(boolean result) {
        System.out.println(this.result);
        if (listener != null) {
            if (!result) listener.onDownloadFailure(this.result);
            else listener.onDownloadComplete(this.result);
        }
    }

    // Слушатель для обработки результата запросов
    public interface NetListener {
        void onDownloadComplete(String msg);

        void onProgressUpdate(int progress);

        void onDownloadFailure(String msg);
    }
}