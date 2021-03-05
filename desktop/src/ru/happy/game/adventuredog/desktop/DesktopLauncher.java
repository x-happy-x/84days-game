package ru.happy.game.adventuredog.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.AssetsTool;

public class DesktopLauncher {
    public static void main(String[] arg) {
        // Конфиг игры
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Путь файла с настройками
        config.setPreferencesConfig("/Desktop/GameData/", Files.FileType.External);
        // Конфигурации дисплея
        int height = 500, width = 1000;
        config.setTitle("Adventure Dog");
        config.setWindowedMode(width,height);
        //config.setWindowPosition((int)(width/2f),(int)(height/2f));
        //config.setWindowSizeLimits(height/10,width/10,height*3,width*3);
        //config.setInitialVisible(true);
        //config.setInitialBackgroundColor(Color.BLACK);
        //config.setResizable(false);
        //config.setMaximized(false);
        //config.setDecorated(true);
        //config.setAutoIconify(true);
        //config.setIdleFPS(60);
        //config.useVsync(true);
        config.setBackBufferConfig(8,8,8,8,16,0,2);
        //config.useOpenGL3(true, GL30.GL_MAJOR_VERSION,GL30.GL_MINOR_VERSION);
        new Lwjgl3Application(new MainGDX(), config);
        MainGDX.write(System.getProperty("user.dir"));
        //load("video/5 (1).mp4");
    }

    public static void load(String... videos){
        Thread t = new Thread(() -> {
            for (String s: videos) createVideo(s);
        });
        t.start();
    }
    private static void createBinary(File directory, String output, float fps) {
        int i;
        File[] files = directory.listFiles();
        ArrayList<File> videoFiles = new ArrayList<>();
        for (i = 0; i < files.length; i++)
            videoFiles.add(files[i]);
        MainGDX.write(videoFiles.size() + "");
        Collections.sort(videoFiles, (file1, file2) -> file1.getName().compareTo(file2.getName()));
        ArrayList<byte[]> videoFilesBytes = new ArrayList<>();
        for (i = 0; i < videoFiles.size(); i++) {
            videoFilesBytes.add(Gdx.files.absolute(videoFiles.get(i).getPath()).readBytes());
        }
        int headerSize = 4 + 4 + (videoFilesBytes.size() * 4 * 2);
        ByteBuffer headerBuffer = ByteBuffer.allocate(headerSize);
        int headerPosition = 0;
        headerBuffer.putFloat(fps);
        headerBuffer.putInt(videoFilesBytes.size());
        for (i = 0; i < videoFilesBytes.size(); i++) {
            headerBuffer.putInt(headerSize + headerPosition);
            headerBuffer.putInt(videoFilesBytes.get(i).length);
            headerPosition += videoFilesBytes.get(i).length;
        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(AssetsTool.getFile(output)))) {
            outputStream.write(headerBuffer.array());
            for (i = 0; i < videoFilesBytes.size(); i++) {
                outputStream.write(videoFilesBytes.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void createVideo(String video) {
        MainGDX.write("LOADING " + video);
        int w = 2048, h = 2048;
        float frames = 24;
        FFmpegFrameGrabber f = new FFmpegFrameGrabber(AssetsTool.getFile(video));
        Java2DFrameConverter c = new Java2DFrameConverter();
        File frame_path = AssetsTool.getFile(new File(video).getParent() + "/" + new File(video).getName() + "_frames/");
        if (frame_path.exists()) {
            AssetsTool.delete(frame_path);
        }
        frame_path.mkdirs();
        try {
            f.start();
            w = f.getImageWidth();
            h = f.getImageHeight();
            frames = (float) f.getVideoFrameRate();
            AssetsTool.ZIP_UNPACK_ALL = f.getLengthInVideoFrames();
            JPEGImageWriteParam jpegParam = new JPEGImageWriteParam(null);
            jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParam.setCompressionQuality(0.5f);
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            BufferedImage frame;
            AssetsTool.ZIP_UNPACK_ALL = f.getLengthInFrames();
            for (AssetsTool.ZIP_UNPACK_COUNT = 0; AssetsTool.ZIP_UNPACK_COUNT < AssetsTool.ZIP_UNPACK_ALL; AssetsTool.ZIP_UNPACK_COUNT++) {
                frame = c.convert(f.grabImage());
                if (frame == null) continue;
                writer.setOutput(new FileImageOutputStream(Gdx.files.absolute(frame_path.getAbsolutePath() + "/frame_" + System.currentTimeMillis() + ".jpg").file()));
                writer.write(null, new IIOImage(frame, null, null), jpegParam);
            }
            f.stop();
        } catch (IOException e) {
            MainGDX.write(e.getLocalizedMessage());
        }
        MainGDX.write("Images created");
        createBinary(frame_path, video.replace(".mp4", ".bin"), frames);
        MainGDX.write("Pack created");
        AssetsTool.getFileHandler(video.replace(".mp4", ".info")).writeString("WIDTH: " + w + "\nHEIGHT: " + h + "\nFPS: " + frames, false);
        AssetsTool.packZip(AssetsTool.getFile(video.replace(".mp4", ".zip")),
                new AssetsTool.ZipPart(
                        AssetsTool.getFile(video.replace(".mp4", ".bin")),
                        "data"),
                new AssetsTool.ZipPart(
                        AssetsTool.getFile(video.replace(".mp4", ".mp3")),
                        "audio.mp3"),
                new AssetsTool.ZipPart(
                        AssetsTool.getFile(video.replace(".mp4", ".info")),
                        "video"));
        AssetsTool.getFileHandler(video.replace(".mp4", ".bin")).delete();
        AssetsTool.getFileHandler(video.replace(".mp4", ".info")).delete();
        AssetsTool.getFileHandler(video.replace(".mp4", ".mp3")).delete();
        AssetsTool.getFileHandler(video).delete();
        AssetsTool.delete(frame_path);
    }
}
