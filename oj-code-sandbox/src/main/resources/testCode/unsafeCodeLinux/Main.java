import java.io.File;
import java.io.FileWriter;

public class Main {
    public static void main(String[] args) {
        try {
            // 尝试在根目录创建文件（应该被 readonlyRootfs 阻止）
            File rootFile = new File("/test.txt");
            if (rootFile.createNewFile()) {
                System.out.println("成功在根目录创建文件！");
            } else {
                System.out.println("无法在根目录创建文件");
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
            // 应该输出类似：Read-only file system
        }

        try {
            // 尝试在 /tmp 目录创建文件（应该成功）
            File tmpFile = new File("/tmp/test.txt");
            if (tmpFile.createNewFile()) {
                System.out.println("成功在 /tmp 创建文件！");
            } else {
                System.out.println("无法在 /tmp 创建文件");
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }

        try {
            // 尝试在 /app 目录创建文件（挂载的目录，应该成功）
            File appFile = new File("/app/test.txt");
            if (appFile.createNewFile()) {
                System.out.println("成功在 /app 创建文件！");
            } else {
                System.out.println("无法在 /app 创建文件");
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }
}