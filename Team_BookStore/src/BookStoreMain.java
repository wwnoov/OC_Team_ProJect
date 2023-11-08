import java.sql.SQLException;

public class BookStoreMain {

    // ------------------------------------------ 로딩 메세지 ------------------------------------- //
    public static void main(String[] args) {
        System.out.println();
        System.out.println("                /$$$$$$$   /$$$$$$   /$$$$$$  /$$   /$$");
        System.out.println("               | $$__  $$ /$$__  $$ /$$__  $$| $$  /$$/");
        System.out.println("               | $$  \\ $$| $$  \\ $$| $$  \\ $$| $$ /$$/");
        System.out.println("               | $$$$$$$ | $$  | $$| $$  | $$| $$$$$/");
        System.out.println("               | $$__  $$| $$  | $$| $$  | $$| $$  $$");
        System.out.println("               | $$  \\ $$| $$  | $$| $$  | $$| $$\\  $$");
        System.out.println("               | $$$$$$$/|  $$$$$$/|  $$$$$$/| $$ \\  $$");
        System.out.println("               |_______/  \\______/  \\______/ |__/  \\__/");
        System.out.println();
        // Thread.sleep를 통해 시간을 두고 출력되게 설정
        try {
            Thread.sleep(700);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("             /$$$$$$  /$$$$$$$$ /$$$$$$  /$$$$$$$  /$$$$$$$$");
        System.out.println("            /$$__  $$|__  $$__//$$__  $$| $$__  $$| $$_____/");
        System.out.println("           | $$  \\__/   | $$  | $$  \\ $$| $$  \\ $$| $$");
        System.out.println("           |  $$$$$$    | $$  | $$  | $$| $$$$$$$/| $$$$$");
        System.out.println("            \\____  $$   | $$  | $$  | $$| $$__  $$| $$__/");
        System.out.println("            /$$  \\ $$   | $$  | $$  | $$| $$  \\ $$| $$");
        System.out.println("           |  $$$$$$/   | $$  |  $$$$$$/| $$  | $$| $$$$$$$$");
        System.out.println("            \\______/    |__/   \\______/ |__/  |__/|________/");
        try {
            Thread.sleep(1500);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println();
        BookStore bookStore = new BookStore();
        bookStore.start();
    }
}
    // ------------------------------------------ 로딩 메세지 ------------------------------------- //