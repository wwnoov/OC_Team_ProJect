import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// BookStore.java


public class BookStore extends DBConnector {

    //어디서든 사용하기 위한 전역 변수 선언
	private Scanner scanner;
    private int loginAttempt; //로그인 시도 횟수
    private String loginId = null; //로그인 값을 저장하는 문자열

    
    public BookStore() {
    	scanner = new Scanner(System.in);
        loginAttempt = 0;
    }


    //홈 화면
    public void start() {
        System.out.println("================================ [홈] ================================");
        System.out.println();
        System.out.println("   │  1.  로 그 인   │    │  2. 회 원 가 입   │   │   0. 종 료    │ ");
        System.out.println();
        System.out.println("======================================================================");
        System.out.println();
        System.out.print("메뉴를 선택 :  ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        System.out.println();

        switch (choice) {
        case 1:
            login(); //로그인 창
            break;
        case 2:
            register(); //회원가입 창
            break;
        case 3:
        	adminMode(); //관리자 모드 창
        	break;
        case 0:
            System.out.println("프로그램을 종료합니다.");
            System.exit(0);
            break;
        default:
            System.out.println("잘못된 메뉴 선택입니다.");
            start();
        }
    }
    

    //로그인 창
    public void login() {
        loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false; // 로그인 여부

        while (!loggedIn && loginAttempt < 3) { //로그인 시도가 3회 미만이면 반복
            Member member = new Member();
            System.out.println("=================================[로그인]=================================");
            System.out.print("아이디: ");
            member.setId(scanner.nextLine());
            System.out.print("비밀번호: ");
            member.setPassword(scanner.nextLine());
            System.out.println("=========================================================================");
            System.out.println();
            try {
                //입력한 아이디의 비밀번호 가져오는 쿼리문
                String query = ""
                		+ "SELECT password FROM member"
                		+ " WHERE id= ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, member.getId());
                ResultSet rs = statement.executeQuery();

                //비밀번호 대조
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(member.getPassword())) {
                        loggedIn = true; // 로그인 성공
                        loginId = member.getId();
                    } else {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                    }
                } else {
                    System.out.println("아이디가 존재하지 않습니다.");
                }
                rs.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
                exit();
                start();
            } finally {
            	if(loginId != null) {
            	System.out.println("              어서오세요, "+loginId+"님. 무엇을 도와드릴까요?");		//loginId 값 부여 확인용
            	}
            }

            loginAttempt++; //비번이 틀리면 로그인 시도 횟수 증가
        }

        if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }
        showMenu();
    }

    //일반 고객 메인 메뉴
    public void showMenu() {
        System.out.println("\n=============================== [메인 메뉴]================================");
        System.out.println();
        System.out.println("   │  1. 도서 구매   │     │  2. 추천 도서   │     │  3. 캐시 충전   │");
        System.out.println();
        System.out.println("             │  4. 후기 게시판   │       │  0. 종료   │  ");
        System.out.println();
        System.out.println("                          보유 잔액: " + (int)getUserCash() + "원");
        System.out.println();
        System.out.println("=========================================================================");
        System.out.println();
        System.out.print("메뉴 선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
	
        switch (choice) {
            case 1:
                //책 구매 페이지
                purchaseBook();
                break;
            case 2:
                //추천 도서
                BestBooks();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showMenu();
                break;
            case 3:
                //캐시 충전
                rechargeCash();
                break;
            case 4:
                //후기 게시판
                boardlist();
                break;
            case 0:
                System.out.println("로그아웃 되었습니다.");
                System.out.println("프로그램을 종료합니다.");
                exit();
            default:
                System.out.println("잘못된 메뉴 선택입니다.");

    	}
    }

    //후기 게시판
    public void boardlist(){
        //타이틀 및 컬럼명 출력
        System.out.println();
        System.out.println("============================[후기 게시판]===============================");
        System.out.printf("%-6s%-12s%-12s%-40s\n", "no", "제목","작성자", "날짜" );
        System.out.println("======================================================================");

        //boads 테이블에서 게시물 정보를 가져와서 출력하기
        try {
            String sql = "" +
                    "SELECT bno, btitle, bcontent, bwriter, bdate " +
                    "FROM boards " +
                    "ORDER BY bno DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Board board = new Board();
                board.setBno(rs.getInt("bno"));
                board.setBtitle(rs.getString("btitle"));
                board.setBcontent(rs.getString("bcontent"));
                board.setBwriter(rs.getString("bwriter"));
                board.setBdate(rs.getDate("bdate"));
                System.out.printf("%-6s%-12s%-12s%-40s\n",
                        board.getBno(),
                        board.getBtitle(),
                        board.getBwriter(),
                        board.getBdate());

            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }

        //메인 메뉴 출력
        boardMenu();
    }

    //후기 게시판의 기능 종류
    public void boardMenu() {
        System.out.println("=====================================================================");
        System.out.println("     │  1. 작성하기   │     │  2. 읽 기   │     │  0. 돌아가기   │");
        System.out.println("=====================================================================");
        System.out.println();
        System.out.print("메뉴 선택: ");
        String menuNo = scanner.nextLine();
        System.out.println();

        switch (menuNo) {
            case "1":
                //새로운 글 작성
                create();
            case "2":
                //게시글 읽기
                read();
            case "0":
                showMenu();
        }
    }

    //새로운 글 작성
    public void create() {
        //입력 받기
        Board board = new Board();
        System.out.println("=============================[새 게시물 입력]=============================");
        System.out.print("제목: ");
        board.setBtitle(scanner.nextLine());
        System.out.print("내용: ");
        board.setBcontent(scanner.nextLine());
        System.out.println("========================================================================");
        board.setBwriter(loginId);

        // 게시판 보조메뉴 출력
        System.out.println("========================================================================");
        System.out.println("               │  1. 저장하기   │        │  0. 취소   │");
        System.out.println("========================================================================");
        System.out.println();
        System.out.print("메뉴선택: ");
        String menuNo = scanner.nextLine();
        if (menuNo.equals("1")) {
            //boards 테이블에 게시물 정보 저장
            try {
                String sql = "" +
                        "INSERT INTO boards (btitle, bcontent, bwriter, bdate) " +
                        "VALUES (?, ?, ?, now())";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, board.getBtitle());
                pstmt.setString(2, board.getBcontent());
                pstmt.setString(3, board.getBwriter());
                pstmt.executeUpdate();
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
                exit();
            }
        }

        //게시물 목록 출력
        boardlist();
    }

    //게시글 읽기
    public void read() {
        //입력 받기
        System.out.println("==============================[게시물 읽기]==============================");
        System.out.print("글 번호 : ");
        int bno = Integer.parseInt(scanner.nextLine());
        System.out.println();
        System.out.println("========================================================================");


        //boards 테이블에서 해당 게시물을 가져와 출력
        try {
            String sql = "" +
                    "SELECT bno, btitle, bcontent, bwriter, bdate " +
                    "FROM boards " +
                    "WHERE bno=?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, bno);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Board board = new Board();
                board.setBno(rs.getInt("bno"));
                board.setBtitle(rs.getString("btitle"));
                board.setBcontent(rs.getString("bcontent"));
                board.setBwriter(rs.getString("bwriter"));
                board.setBdate(rs.getDate("bdate"));
                System.out.println("번호: " + board.getBno());
                System.out.println("제목: " + board.getBtitle());
                System.out.println("내용: " + board.getBcontent());
                System.out.println("쓴이: " + board.getBwriter());
                System.out.println("날짜: " + board.getBdate());

                //관리자일 경우 글 수정 기능이 필요 없기 때문에 삭제하기만 출력
                if (loginId.equals("admin")){
                    System.out.println("========================================================================");
                    System.out.println("               │  1. 삭제하기   │        │  0. 취소   │");
                    System.out.println("========================================================================");
                    System.out.println();
                    System.out.print("메뉴선택: ");
                    String menuNo = scanner.nextLine();
                    System.out.println();

                    if(menuNo.equals("1")){
                        delete(board);
                    }
                }else {
                    //보조메뉴 출력
                    System.out.println("========================================================================");
                    System.out.println("     │  1. 수정하기   │     │  2. 삭제하기   │     │  0. 돌아가기   │");
                    System.out.println("========================================================================");
                    System.out.println();
                    System.out.print("메뉴선택: ");
                    String menuNo = scanner.nextLine();
                    System.out.println();

                    if (menuNo.equals("1")) {
                        //작성자 본인만 글 수정 가능
                        if (loginId.equals(board.getBwriter())) {
                            update(board);
                        } else {
                            System.out.println("작성자 본인만 수정 가능합니다.");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (menuNo.equals("2")) {
                        //작성자 본인만 삭제 가능
                        if (loginId.equals(board.getBwriter()) || loginId.equals("admin")) {
                            delete(board);
                        } else {
                            System.out.println("작성자 본인만 삭제 가능합니다.");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }

        if(loginId.equals("admin")){
            boardAdmin();
        }else {
            //게시물 목록 출력
            boardlist();
        }
    }

    //글 수정 기능
    public void update(Board board) {
        //수정 내용 입력 받기
        System.out.println("==============================[수정 내용 입력]==============================");
        System.out.print("제목 : ");
        board.setBtitle(scanner.nextLine());
        System.out.print("내용 : ");
        board.setBcontent(scanner.nextLine());
        System.out.println("=========================================================================");

        //보조메뉴 출력
        System.out.println("               │  1. 수정하기   │        │  0. 취소   │");
        System.out.println("=========================================================================");
        System.out.println();
        System.out.print("메뉴선택: ");
        System.out.println();
        String menuNo = scanner.nextLine();
        if (menuNo.equals("1")) {
            //boards 테이블에서 게시물 정보 수정
            try {
                String sql = "" +
                        "UPDATE boards SET btitle=?, bcontent=? " +
                        "WHERE bno=?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, board.getBtitle());
                pstmt.setString(2, board.getBcontent());
                pstmt.setInt(3, board.getBno());
                pstmt.executeUpdate();
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
                exit();
            }
        }

        //게시물 목록 출력
        boardlist();
    }

    //관리자 모드 - 후기 게시판
    public void boardAdmin(){

        System.out.println();
        System.out.println("============================[관리자 모드]===============================");
        System.out.printf("%-6s%-12s%-12s%-40s\n", "no", "제목","작성자", "날짜" );
        System.out.println("======================================================================");

        //boads 테이블에서 게시물 정보를 가져와서 출력하기
        try {
            String sql = "" +
                    "SELECT bno, btitle, bcontent, bwriter, bdate " +
                    "FROM boards " +
                    "ORDER BY bno DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Board board = new Board();
                board.setBno(rs.getInt("bno"));
                board.setBtitle(rs.getString("btitle"));
                board.setBcontent(rs.getString("bcontent"));
                board.setBwriter(rs.getString("bwriter"));
                board.setBdate(rs.getDate("bdate"));
                System.out.printf("%-6s%-12s%-12s%-40s\n",
                        board.getBno(),
                        board.getBtitle(),
                        board.getBwriter(),
                        board.getBdate());

            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }
        boardAdminMenu();
    }

    //관리자 모드 - 후기 게시판 - 기능
    public void boardAdminMenu(){
        System.out.println();
        loginId = "admin";
        System.out.println("======================================================================");
        System.out.println("     │  1. 읽기   │     │  2. 삭제하기   │     │  0. 돌아가기   │");
        System.out.println("======================================================================");
        System.out.println();
        System.out.print("메뉴선택: ");
        int menuNo = Integer.parseInt(scanner.nextLine());
        System.out.println();
        switch (menuNo) {
            case 1:
                //글 읽기
                read();
            case 2:
                //글 삭제
                deleteBoard();
            case 0:
                adminMenu();
            default:
                System.out.println("잘못된 입력입니다");
        }
    }

    //특정 번호의 게시물 삭제
    public void deleteBoard(){
        
        System.out.print("삭제할 게시물 번호를 입력 :");
        String deleteBoard = scanner.nextLine();
      
        try {
            String sql = "DELETE FROM boards WHERE bno=?" ;
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1,deleteBoard);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
        System.out.println(deleteBoard+"번 게시글이 삭제되었습니다.");

        if (loginId.equals("admin")){
            boardAdmin();
        }else {
            //게시물 목록 출력
            boardlist();
        }
    }

    //게시글 읽기 중 삭제는 들어와있는 게시글을 삭제하므로 번호 입력 불필요
    public void delete(Board board) {
        //boards 테이블에 게시물 정보 삭제
        try {
            String sql = "DELETE FROM boards WHERE bno=?" ;
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, board.getBno());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
        if (loginId.equals("admin")){
            boardAdmin();
        }else {
            //게시물 목록 출력
            boardlist();
        }
    }

    //프로그램 종료
    public void exit() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        System.exit(0);
    }

    //원하는 카테고리 선택을 위한 도서 종류 페이지
    public void gradeBooks(){
        System.out.println("\n==============================[도서 종류]===============================");
        System.out.println();
        System.out.println("   │  1. 초등학교   │     │  2. 중학교   │     │  3. 고등학교   │");
        System.out.println();
        System.out.println("             │  4. 교과서   │       │  0. 돌아가기   │  ");
        System.out.println();
        System.out.println("======================================================================");
        System.out.println();
        System.out.print("종류 선택: ");
        String bookNums = scanner.nextLine();
        System.out.println();

        switch (bookNums) {
            case "1":
                //초등학교
                ElementaryBooks();
                break;
            case "2":
                //중학교
                MiddleBooks();
                break;
            case "3":
                //고등학교
                HighBooks();
                break;
            case "4":
                //교과서
                Textbooks();
                break;
            case "0":
                showMenu();
                break;
        }
    }

    //초등학교 책 목록
    public void ElementaryBooks() {
        try {
            String query = ""+
                    "SELECT book_id, book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "WHERE grade='초등' "+
                    "ORDER BY book_id ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("======================================================================");
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
            }
            resultSet.close();
        }catch(SQLException e) {
            e.printStackTrace();
            System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }

    //중학교 책 목록
    public void MiddleBooks() {
        try {
            String query = ""+
                    "SELECT book_id, book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "WHERE grade='중등' "+
                    "ORDER BY book_id ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("======================================================================");
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
            }
            resultSet.close();
        }catch(SQLException e) {
            e.printStackTrace();
            System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }

    //고등학교 책 목록
    public void HighBooks() {
        try {
            String query = ""+
                    "SELECT book_id, book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "WHERE grade='고등' "+
                    "ORDER BY book_id ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("======================================================================");
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
            }
            resultSet.close();
        }catch(SQLException e) {
            e.printStackTrace();
            System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }

    //교과서 목록
    public void Textbooks() {
        try {
            String query = ""+
                    "SELECT book_id, book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "WHERE grade='교과서' "+
                    "ORDER BY book_id ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("======================================================================");
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
            }
            resultSet.close();
        }catch(SQLException e) {
            e.printStackTrace();
            System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }

    //책 목록
    public void displayBooks() {
        try {
			String query = ""+
					"SELECT book_id, book_name,author, price, grade, quantity " +
					"FROM book "+
					"ORDER BY grade ASC";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
                System.out.println("======================================================================");
				Book book = new Book();
				book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
            }
            System.out.println("======================================================================");
			resultSet.close();
        }catch(SQLException e) {
        	e.printStackTrace();
        	System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }

    //추천 도서 - 재고가 적은순
    public void BestBooks() {
        try {
            String query = ""+
                    "SELECT book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "ORDER BY quantity ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("================================[추천 도서]================================");
            while (resultSet.next()) {
                Book book = new Book();
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream print = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 카테고리 :%s | 재고: %d | \n",
                        book.getBook_name(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getGrade(),
                        book.getQuantity()
                );
                System.out.println("==========================================================================");
            }
            try {
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
            resultSet.close();
        }catch(SQLException e) {
            e.printStackTrace();
            System.out.println("문제가 발생하였습니다. 관리자에게 문의해주세요.");
        }
    }
    
    //책 구매 페이지
    public void purchaseBook() {
        // Display all book information
        int totalPrice = 0;

 		List<String> bookNames = new ArrayList<>();
 		List<Integer> quantities = new ArrayList<>();

        gradeBooks();
        System.out.println("======================================================================");
        System.out.print("구매할 도서 이름을 입력하세요: ");
        String bookName = scanner.nextLine();
        System.out.print("구매 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
        System.out.println("======================================================================");
        scanner.nextLine(); // 버퍼 비우기
        bookNames.add(bookName);
        quantities.add(quantity);

 		
 		try {
 		    connection.setAutoCommit(false);

 		    String query1 = "SELECT price, quantity "
 		            + "FROM book "
 		            + "WHERE book_name = ?";
 		    PreparedStatement statement1 = connection.prepareStatement(query1);

 		    String query2 = "UPDATE book SET quantity = quantity - ? WHERE book_name = ?";

 		    PreparedStatement statement2 = connection.prepareStatement(query2);

            String query3 = "UPDATE member "
                    + "SET cash = cash - ? "
                    + "WHERE id = ?";
            PreparedStatement statement3 = connection.prepareStatement(query3);



            //구매한 책에 대한 영수증
            System.out.println("\n===============================[영수증]===============================");
 		    for (int i = 0; i < bookNames.size(); i++) {
                bookName = bookNames.get(i);
 		        quantity = quantities.get(i);

 		        statement1.setString(1, bookName);
 		        ResultSet resultSet = statement1.executeQuery();
	            
 		        if (resultSet.next()) {
 		            int price = resultSet.getInt("price");
 		            int availableQuantity = resultSet.getInt("quantity");

 		            if (availableQuantity < quantity) {
 		                throw new Exception("수량이 맞지 않습니다. 재고를 확인해주세요.");
 		            }

 		            totalPrice += (price * quantity);

 		            // 현재 시간을 얻기 위한 날짜 포맷 지정
 		            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		            String currentTime = dateFormat.format(new Date());

 		            System.out.println("구매 도서 이름: " + bookName);
 		            System.out.println("구매 수량: " + quantity);
 		            System.out.println("총 가격: " + (price * quantity)+"원");
 		            System.out.println("구매 시간: " + currentTime);
 		            System.out.println("=====================================================================");

                    try {
                        Thread.sleep(2000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

 		            statement2.setInt(1, quantity);
 		            statement2.setString(2, bookName);
 		            statement2.executeUpdate();
 		        } else {
 		            throw new Exception("도서를 찾을 수 없습니다.");
                }
 		    }

 		    statement3.setDouble(1, totalPrice);
 		    statement3.setString(2, loginId);
 		    int rows3 = statement3.executeUpdate();
 		    if (rows3 == 0) {
 		        throw new Exception("잔액이 부족합니다.");
 		    }

 		    connection.commit();
 		}catch (Exception e) {
 			e.printStackTrace();
 			try {
 				connection.rollback();
 			}catch (SQLException e1) {
 				System.out.println("구매하기에 실패하셨습니다. 원하시는 도서의 재고를 확인해주세요.");
 			}
 		}finally {
 			if(connection != null) {
 				try {
 					connection.setAutoCommit(true);
 				} catch (SQLException e2) {}
 			}
            System.out.println("캐시 잔액 : " + (int)getUserCash() + "원");
 			showMenu();
 		}
    }
    
    
    
    //캐시 충전 페이지
    public void rechargeCash() {
        System.out.println("================================[캐시 충전]================================");
        System.out.println("보유 캐시: " + (int)getUserCash() + "원");
        System.out.print("충전할 금액을 입력하세요: ");
        double cash = Double.parseDouble(scanner.nextLine());

        if (updateUserCash(getUserCash() + cash)) {
            System.out.println("캐시를 충전하였습니다.");
            System.out.println("보유 캐시: " + (int)getUserCash() + "원");
        } else {
            System.out.println("캐시 충전에 실패했습니다.");
        }
        System.out.println("=========================================================================");
        showMenu();
    }


    //일반 고객의 보유 캐시
    private double getUserCash() {
        try {
        	String query = "SELECT cash FROM member WHERE id=?";
        	PreparedStatement statement=connection.prepareStatement(query);
        	statement.setString(1, loginId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double cash = resultSet.getDouble("cash");
                resultSet.close();
                return cash;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    //캐시 충전을 하여 보유 캐시 변경을 위한 기능
    private boolean updateUserCash(double cash) {
        try {
            String query = "UPDATE member"
            		+ " SET cash = ?"
            		+ " WHERE id = ?";
        	PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, cash);
            statement.setString(2, loginId);
            int updatedRows = statement.executeUpdate();
            statement.close();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    //회원 가입 페이지
    private void register() {
    	Member member = new Member();
        while (true){
            System.out.println("==============================[회원 가입]==============================");
            System.out.print("사용할 아이디를 입력하세요: ");
            member.id = scanner.nextLine();
            if (member.id.equals("admin")){
                System.out.println("'admin' 이라는 아이디는 사용할 수 없습니다.");
            }else {
                break;
            }

        }

        System.out.print("사용할 비밀번호를 입력하세요: ");
        member.password = scanner.nextLine();

        if (isIdAvailable(member.id)) {
            try {
            	String query = "" +
            			"INSERT INTO member (id, password, cash)"+
            			" VALUES (?, ?, ?)";
            	PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, member.getId());
                statement.setString(2, member.getPassword());
                statement.setInt(3, 0); // 초기 보유 현금은 0으로 설정
                statement.executeUpdate();
                statement.close();    
                System.out.println("회원가입에 성공했습니다.");
           }catch (SQLException e) {
               e.printStackTrace();
               System.out.println("회원가입에 실패했습니다.");
           }
        }else {
            System.out.println("이미 사용 중인 아이디입니다. 다른 아이디를 선택해주세요.");
        }
        System.out.println("=====================================================================");
        System.out.println();
        start();
    }

    //회원가입에 입력한 아이디 중복 여부 체크
    private boolean isIdAvailable(String id) {
        try {
        	String query = "" +
        			"SELECT * FROM member"+
        			" WHERE id = ?";
        	PreparedStatement statement = connection.prepareStatement(query);
        	statement.setString(1, id);
        	ResultSet resultSet = statement.executeQuery();
            boolean isAvailable = !resultSet.next(); // 이미 존재하는 경우 false 반환
            resultSet.close();
            return isAvailable;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //관리자 로그인
    private void adminMode() {
    	loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false;
    	
        while(!loggedIn && loginAttempt < 3) {
        	Admin admin = new Admin();
            System.out.println("============================[관리자 로그인]============================");
            System.out.println();
        	System.out.print("관리자 아이디를 입력하세요: ");
        	admin.setAdId(scanner.nextLine());
        	System.out.print("관리자 비밀번호를 입력하세요.: ");
        	admin.setAdPassword(scanner.nextLine());
            System.out.println();
            System.out.println("=====================================================================");
            System.out.println();

        	try {
				String query = ""
						+ "SELECT password FROM admin"
						+ " WHERE id= ?";
				PreparedStatement statement=connection.prepareStatement(query);
				statement.setString(1, admin.getAdId());
				ResultSet resultSet=statement.executeQuery();
				if(resultSet.next()) {
					String dbPassword = resultSet.getString("password");
                    if (dbPassword.equals(admin.getAdPassword())) {
                        loggedIn = true; // 로그인 성공
                        loginId = admin.getAdId();
                        System.out.println("관리자 모드로 전환되었습니다.");
                        adminMenu();
                    } else {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                    }
                } else {
                    System.out.println("아이디가 존재하지 않습니다.");
                }
				
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("관리자 시스템에 문제가 발생하여 관리자 모드를 종료합니다.");
				start();
			}
            System.out.println("=====================================================================");
            loginAttempt++; // 로그인 시도 횟수 증가
        }
        
    	if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }
    }

    //관리자 모드 - 메인 메뉴
    private void adminMenu(){
        while (true) {
            System.out.println("\n=============================[관리자 메뉴]=============================");
            System.out.println();
            System.out.println("             │  1. 재고 관리   │        │  2. 재고 확인   │");
            System.out.println();
            System.out.println("            │  3. 게시판 관리   │       │  0. 관리자 종료  │  ");
            System.out.println();
            System.out.println("=====================================================================");
            System.out.print("메뉴 선택: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            switch (choice) {
                case 1:
                    //재고 관리
                    manageStock();
                    break;
                case 2:
                    //재고 확인
                    displayBooks();
                    try {
                        Thread.sleep(2000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    //게시판 관리
                    boardAdmin();
                    break;
                case 0:
                    System.out.println("관리자 모드를 종료합니다.");
                    start();
                    break;
                default:
                    System.out.println("잘못된 메뉴 선택입니다.");
            }
        }
    }

    //관리자 - 재고 관리 페이지
    private void manageStock(){
        displayBooks();
        System.out.println("\n==============================[재고 관리]=============================");
        System.out.println();
        System.out.println("          │  1. 새 책 추가하기   │       │  2. 책 삭제하기   │");
        System.out.println();
        System.out.println("          │  3. 책 재고 채우기   │        │  0. 돌아가기   │  ");
        System.out.println();
        System.out.println("=====================================================================");
        System.out.print("메뉴 선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        switch (choice) {
            case 1:
                //새 책 추가
                createBook();
                break;
            case 2:
                //책 삭제
                deleteBook();
                break;
            case 3:
                //재고 추가
                fillStock();
                break;
            case 0:
                adminMenu();
                break;
            default:
                System.out.println("잘못된 메뉴 선택입니다.");
        }
    }

    //관리자 - 이미 있는 책의 재고 추가
    private void fillStock() {
        System.out.println("\n==============================[재고 추가]==============================");
        System.out.print("재고를 추가할 도서 이름을 입력하세요: ");
        String bookName = scanner.nextLine();
        System.out.print("추가할 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        System.out.println("=======================================================================");
        System.out.println();

        try {
            String query = ""
            		+ "UPDATE book"
            		+ " SET quantity = quantity + ?"
            		+ " WHERE book_name = ?";
        	PreparedStatement statement= connection.prepareStatement(query);
        	statement.setInt(1, quantity);
        	statement.setString(2, bookName);
            int rowsUpdated = statement.executeUpdate();
            statement.close();
            if (rowsUpdated > 0) {
                System.out.println("재고를 추가하였습니다.");
            } else {
                System.out.println("도서 이름을 확인하세요.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        manageStock();
    }

    //관리자 - 새로운 책 추가
    public void createBook() {
        //입력 받기
        Book book = new Book();
        System.out.println("=============================[새 책 추가하기]=============================");
        System.out.print("도서 이름: ");
        book.setBook_name(scanner.nextLine());
        System.out.print("저자: ");
        book.setAuthor(scanner.nextLine());
        System.out.print("가격: ");
        book.setPrice(Integer.parseInt(scanner.nextLine()));
        System.out.print("카테고리: ");
        book.setGrade(scanner.nextLine());
        System.out.print("재고: ");
        book.setQuantity(Integer.parseInt(scanner.nextLine()));
        System.out.println("========================================================================");

        // 게시판 보조메뉴 출력
        System.out.println("========================================================================");
        System.out.println("               │  1. 추가하기   │        │  0. 취소   │");
        System.out.println("========================================================================");
        System.out.println();
        System.out.print("메뉴선택: ");
        String menuNo = scanner.nextLine();
        if (menuNo.equals("1")) {
            //boards 테이블에 게시물 정보 저장
            try {
                String sql = "" +
                        "INSERT INTO book (book_name, author, price, grade, quantity) " +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, book.getBook_name());
                pstmt.setString(2, book.getAuthor());
                pstmt.setInt(3, book.getPrice());
                pstmt.setString(4, book.getGrade());
                pstmt.setInt(5, book.getQuantity());
                pstmt.executeUpdate();
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
                exit();
            }
        }
        //게시물 목록 출력
        manageStock();
    }

    //관리자 - 책 삭제
    public void deleteBook(){
        System.out.println("==============================[책 삭제하기]==============================");
        System.out.print("삭제할 책 이름 입력 :");
        String deleteBook = scanner.nextLine();
        System.out.println("=======================================================================");
        try {
            String sql = "DELETE FROM book WHERE book_name=?" ;
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1,deleteBook);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
        System.out.println("'"+deleteBook+"' 책이 삭제되었습니다.");

        manageStock();
    }
}
