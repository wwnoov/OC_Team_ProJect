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
	private Scanner scanner;
    private int loginAttempt;    // 로그인 시도 횟수
    private String loginId = null; //로그인 값을 저장하는 문자열

    
    public BookStore() {
    	scanner = new Scanner(System.in);
        loginAttempt = 0;
    }
    // 메인 화면
    public void start() {
    	System.out.println(" ");
        System.out.println("========== BOOK STORE ==========");
        System.out.println("1. 로그인");
        System.out.println("2. 회원가입");
        System.out.println("3. 관리자모드");
        System.out.println("0. 종료");
        System.out.print("메뉴 선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        switch (choice) {
        case 1:
            login();        // 로그인
            break;
        case 2:
            register();     // 회원가입
            break;
        case 3:
        	adminMode();     //관리자모드
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
    
	// 로그인
    public void login() {
        loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false; // 로그인 여부

        while (!loggedIn && loginAttempt < 3) {
            Member member = new Member();
            System.out.println("[로그인]");
            System.out.print("아이디: ");
            member.setId(scanner.nextLine());
            System.out.print("비밀번호: ");
            member.setPassword(scanner.nextLine());

            try {
                // 로그인시 아이디 비밀번호 체크
                String query = ""
                		+ "SELECT password FROM member"
                		+ " WHERE id= ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, member.getId());
                ResultSet rs = statement.executeQuery();
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
            	System.out.println("어서오세요, "+loginId+"님. 무엇을 도와드릴까요?");		//loginId 값 부여 확인용
            	}
            }

            loginAttempt++; // 로그인 시도 횟수 증가
        }

        if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }

        showMenu();
    }
     // 메인메뉴
    public void showMenu() {
    	
	        System.out.println("\n========== 메뉴 ==========");
	        System.out.println("1. 도서 구매");
	        System.out.println("2. 추천 도서");
	        System.out.println("3. 캐시 충전");
            System.out.println("4. 후기 게시판");
	        System.out.println("0. 종료");
	        System.out.print("메뉴 선택: ");
	        int choice = scanner.nextInt();
	        scanner.nextLine(); // 버퍼 비우기
	
	        switch (choice) {
	            case 1:
	                purchaseBook();      //도서 구매
	                break;
	            case 2:
                    BestBooks();     // 추천 도서
	                showMenu();
	                break;
	            case 3:
	                rechargeCash();     // 캐시 충전
	                break;
                case 4:
                    boardlist();        // 후기 게시판
                    break;
	            case 0:
	                System.out.println("로그아웃 되었습니다.");
                    System.out.println("프로그램을 종료합니다.");
	                exit();
	            default:
	                System.out.println("잘못된 메뉴 선택입니다.");
	        
    	}
    }
    // 후기게시판
    public void boardlist(){
        //타이틀 및 컬럼명 출력
        System.out.println();
        System.out.println("[게시물 목록]");
        System.out.println("==========================================");
        System.out.printf("%-6s%-12s%-12s%-40s\n", "no", "제목","작성자", "날짜" );
        System.out.println("==========================================");

        // boads 테이블에서 게시물 정보를 가져와서 출력하기
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
    
    // 후기 게시판 메뉴
    public void boardMenu() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("메인메뉴: 1.등록 | 2.읽기 | 0.홈 메뉴");
        System.out.print("메뉴선택: ");
        String menuNo = scanner.nextLine();
        System.out.println();

        switch (menuNo) {
            case "1":
                create();
            case "2":
                read();
            case "0":
                showMenu();
        }
    } // 게시판 글 생성
    public void create() {
        //입력 받기
        Board board = new Board();
        System.out.println("[새 게시물 입력]");
        System.out.print("제목: ");
        board.setBtitle(scanner.nextLine());
        System.out.print("내용: ");
        board.setBcontent(scanner.nextLine());
        System.out.print("글쓴이: ");
        board.setBwriter(scanner.nextLine());

        // 게시판 보조메뉴 출력
        System.out.println("==========================================");
        System.out.println("보조메뉴: 1.저장 | 2.취소");
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
     // 게시판 글 읽기
    public void read() {
        //입력 받기
        System.out.println("[게시물 읽기]");
        System.out.print("bno: ");
        int bno = Integer.parseInt(scanner.nextLine());

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
                System.out.println("#############");
                System.out.println("번호: " + board.getBno());
                System.out.println("제목: " + board.getBtitle());
                System.out.println("내용: " + board.getBcontent());
                System.out.println("쓴이: " + board.getBwriter());
                System.out.println("날짜: " + board.getBdate());
                //보조메뉴 출력
                System.out.println("==========================================");
                System.out.println("보조메뉴: 1.수정 | 2.삭제 | 0.이전메뉴");
                System.out.print("메뉴선택: ");
                String menuNo = scanner.nextLine();
                System.out.println();

                if (menuNo.equals("1")) {
                    update(board);
                } else if (menuNo.equals("2")) {
                    delete(board);
                }
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }

        //게시물 목록 출력
        boardlist();
    }
    // 게시글 수정
    public void update(Board board) {
        //수정 내용 입력 받기
        System.out.println("[수정 내용 입력]");
        System.out.print("제목: ");
        board.setBtitle(scanner.nextLine());
        System.out.print("내용: ");
        board.setBcontent(scanner.nextLine());
        System.out.print("글쓴이: ");
        board.setBwriter(scanner.nextLine());

        //보조메뉴 출력
        System.out.println("==========================================");
        System.out.println("보조메뉴: 1.Ok | 2.Cancel");
        System.out.print("메뉴선택: ");
        String menuNo = scanner.nextLine();
        if (menuNo.equals("1")) {
            //boards 테이블에서 게시물 정보 수정
            try {
                String sql = "" +
                        "UPDATE boards SET btitle=?, bcontent=?, bwriter=? " +
                        "WHERE bno=?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, board.getBtitle());
                pstmt.setString(2, board.getBcontent());
                pstmt.setString(3, board.getBwriter());
                pstmt.setInt(4, board.getBno());
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
    // 게시판 관리
    public void boardAdmin(){

        System.out.println();
        System.out.println("[게시물 목록]");
        System.out.println("==========================================");
        System.out.printf("%-6s%-12s%-12s%-40s\n", "no", "제목","작성자", "날짜" );
        System.out.println("==========================================");

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
    // 게시판 관리 메인메 뉴
    public void boardAdminMenu(){
        System.out.println();
        System.out.println("==========================================");
        System.out.println("메인메뉴: 1.읽기 | 2.삭제 | 0.홈 메뉴");
        System.out.print("메뉴선택: ");
        String menuNo = scanner.nextLine();
        System.out.println();

        switch (menuNo) {
            case "1":
                read();
            case "2":
                deleteBoard();
            case "0":
                showMenu();
        }
    }
    // 게시글 삭제
    public void deleteBoard(){
        
        System.out.println("삭제할 게시물 번호를 입력해주세요:");
        int deleteBoard = scanner.nextInt();
      
        try {
            String sql = "DELETE FROM boards WHERE bno=?" ;
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1,deleteBoard);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
        System.out.println(deleteBoard+"번 게시글이 삭제되었습니다.");

        start();
    }
    // 게시글 삭제 (본인인지 체크필요)
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

        //게시물 목록 출력
        boardlist();
    }
    public void exit() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        System.exit(0);
    }
    // 학년별 도서 메뉴
    public void gradeBooks(){
        System.out.println("도서목록: 1.초등 | 2.중등 | 3.고등 | 4.교과서 |  0.홈 메뉴");
        System.out.print("목록선택: ");
        String bookNums = scanner.nextLine();
        System.out.println();

        switch (bookNums) {
            case "1":
                ElementaryBooks();
                break;
            case "2":
                MiddleBooks();
                break;
            case "3":
                HighBooks();
                break;
            case "4":
                Textbooks();
                break;
            case "0":
                showMenu();
                break;
        }
    }
    // 초등 메뉴
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
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 재고: %d | \n",
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
    // 중등 메뉴
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
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 재고: %d | \n",
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
    // 고등 메뉴
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
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 재고: %d | \n",
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
    // 교과서 메뉴
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
                Book book = new Book();
                book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 재고: %d | \n",
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
    // 도서 목록
    public void displayBooks() {
        try {
			String query = ""+
					"SELECT book_id, book_name,author, price, grade, quantity " +
					"FROM book "+
					"ORDER BY book_id ASC";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				Book book = new Book();
				book.setBook_id(resultSet.getInt("book_id"));
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream printf = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 재고: %d | \n",
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
    // 추천 도서 목록
    public void BestBooks() {
        try {
            // 추천도서를 재고량 순으로 정렬하여 판매량으로 볼 수 있도록 쿼리문 제작 (디폴트 재고량이 같은기준)
            String query = ""+
                    "SELECT book_name,author, price, grade, quantity " +
                    "FROM book "+
                    "ORDER BY quantity DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("================ 추천 도서 ==================");
            while (resultSet.next()) {
                Book book = new Book();
                book.setBook_name(resultSet.getString("book_name"));
                book.setAuthor(resultSet.getString("author"));
                book.setPrice(resultSet.getInt("price"));
                book.setGrade(resultSet.getString("grade"));
                book.setQuantity(resultSet.getInt("quantity"));
                PrintStream print = System.out.printf("도서 이름: %s | 저자:%s | 가격: %d | 학년 :%s | 판매량 :%d | \n",
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
    
    
    // 도서 구매
    public void purchaseBook() {
        // Display all book information
     	
        int totalPrice = 0;
    	

 		List<String> bookNames = new ArrayList<>();
 		List<Integer> quantities = new ArrayList<>();

 		
 		
 		System.out.println("====================도서 목록 및 재고====================");
        gradeBooks();

        System.out.print("구매할 도서 이름을 입력하세요: ");
        String bookName = scanner.nextLine();
        System.out.print("구매 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
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



            // 구매한 도서에 대한 영수증
            System.out.println("\n========== 영수증 ==========");
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
 		            System.out.println("============================");

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
            System.out.println("보유 캐시: " + (int)getUserCash() + "원");
 			showMenu();
 		}
    }
    
     
    // 캐시 충전 메뉴
    
    public void rechargeCash() {
        System.out.println("보유 캐시: " + (int)getUserCash() + "원");
        System.out.print("충전할 금액을 입력하세요: ");
        double cash = Double.parseDouble(scanner.nextLine());

        if (updateUserCash(getUserCash() + cash)) {
            System.out.println("캐시를 충전하였습니다.");
            System.out.println("보유 캐시: " + (int)getUserCash() + "원");
        } else {
            System.out.println("캐시 충전에 실패했습니다.");
        }
        showMenu();
    }

    // 캐시 보유 현황

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
        
    // 캐시 충전 쿼리문
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
    
    
    
    // 회원 가입 메뉴
    private void register() {
    	Member member = new Member();
        System.out.print("사용할 아이디를 입력하세요: ");
        member.id = scanner.nextLine();
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
        start();
    }
    // 회원가입시 아이디 중복체크
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
    
    // 관리자 모드 메뉴
    private void adminMode() {
    	loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false;
    	
        while(!loggedIn && loginAttempt < 3) {
        	Admin admin = new Admin();
        	System.out.print("관리자 아이디를 입력하세요: ");
        	admin.setAdId(scanner.nextLine());
        	System.out.print("관리자 비밀번호를 입력하세요.: ");
        	admin.setAdPassword(scanner.nextLine());
        	
        	try {
                // 기존에 정해둔 관리자 아이디비밀번호 체크
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
                        
                        while (true) {
                            System.out.println("\n========== 관리자 모드 ==========");
                            System.out.println("1. 재고 채우기");
                            System.out.println("2. 재고 확인");
                            System.out.println("3. 게시판 관리");
                            System.out.println("0. 돌아가기");
                            System.out.print("메뉴 선택: ");
                            int choice = scanner.nextInt();
                            scanner.nextLine(); // 버퍼 비우기

                            switch (choice) {
                                case 1:
                                    fillStock();
                                    break;
                                case 2:
                                    displayBooks();
                                    break;
                                case 3:
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
        	
            loginAttempt++; // 로그인 시도 횟수 증가
        }
        
    	if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }
    }

    // 도서 재고 추가 (관리자모드)
    private void fillStock() {
        System.out.print("추가할 도서 이름을 입력하세요: ");
        String bookName = scanner.nextLine();
        System.out.print("추가할 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

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
    }
    
    
}
