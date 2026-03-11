public class Main {
    public static void main(String[] args) {
        try {
            // 休眠10秒，模拟耗时操作
            Thread.sleep(11000);
            System.out.println("休眠结束");
        } catch (InterruptedException e) {
            System.out.println("休眠被中断");
        }
    }
}