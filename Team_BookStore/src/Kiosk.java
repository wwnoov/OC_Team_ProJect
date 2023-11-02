import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// Kiosk.java


public class Kiosk extends DBConnector {
	private Scanner scanner;				//Kiosk() 생산자에서 필드전역 선언으로 이동
    private int loginAttempt;
    private String loginId=null; //로그인 값을 저장하는 문자열

    
    public Kiosk() {
    	scanner = new Scanner(System.in);
        loginAttempt = 0;
    }

    public void start() {
    	System.out.println(" ");
        System.out.println("========== Kiosk ==========");
        System.out.println("1. 로그인");
        System.out.println("2. 회원가입");
        System.out.println("0. 종료");
        System.out.print("메뉴 선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        switch (choice) {
        case 1:
            login();
            break;
        case 2:
            register();
            break;
        case 3:
        	adminMode();
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
    
	
    public void login() {
        loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false; // 로그인 여부

        while (!loggedIn && loginAttempt < 3) {
            KMember kmember = new KMember();
            System.out.println("[로그인]");
            System.out.print("아이디: ");
            kmember.setId(scanner.nextLine());
            System.out.print("비밀번호: ");
            kmember.setPassword(scanner.nextLine());

            try {
                String query = ""
                		+ "SELECT password FROM k_member"
                		+ " WHERE id= ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, kmember.getId());
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(kmember.getPassword())) {
                        loggedIn = true; // 로그인 성공
                        loginId = kmember.getId();
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

    private void showMenu() {
    	
	        System.out.println("\n========== 메뉴 ==========");
	        System.out.println("1. 물품 구매");
	        System.out.println("2. 재고 확인");
	        System.out.println("3. 현금 충전");
	        System.out.println("0. 로그아웃");
	        System.out.print("메뉴 선택: ");
	        int choice = scanner.nextInt();
	        scanner.nextLine(); // 버퍼 비우기
	
	        switch (choice) {
	            case 1:
	                purchaseProduct();
	                break;
	            case 2:
	                displayProducts();
	                showMenu();
	                break;
	            case 3:
	                rechargeCash();
	                break;
	            case 0:
	                System.out.println("로그아웃 되었습니다.");
	                start();
	            default:
	                System.out.println("잘못된 메뉴 선택입니다.");
	        
    	}
    }

    
    public void displayProducts() {
        try {
			String query = ""+
					"SELECT product_id, product_name, price, quantity " +
					"FROM product "+
					"ORDER BY product_id ASC";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();
			System.out.println("================ 상품 목록 ================");
			while (resultSet.next()) {
				Product product = new Product();
				product.setProduct_id(resultSet.getInt("product_id"));
				product.setProduct_name(resultSet.getString("product_name"));
				product.setPrice(resultSet.getInt("price"));
				product.setQuantity(resultSet.getInt("quantity"));
				System.out.printf("상품 ID: %d, 상품명: %s, 가격: %d, 재고: %d\n",
									product.getProduct_id(),
									product.getProduct_name(),
									product.getPrice(),
									product.getQuantity()
					        		);
			}
			resultSet.close();
        }catch(SQLException e) {
        	e.printStackTrace();
        	System.out.println("문제가 발생하여 재고를 확인하실 수 없습니다. 카운터에 문의해주세요.");
        }
    }
    
    

    public void purchaseProduct() {
        // Display all product information
     	
        int totalPrice = 0;
    	

 		List<Integer> productIds = new ArrayList<>();
 		List<Integer> quantities = new ArrayList<>();

 		
 		
 		System.out.println("**전체 상품 목록 및 재고**");
 		displayProducts();
 		
        System.out.print("구매할 품목 개수를 입력하세요:");
        int itemCount = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        
 		for (int i = 0; i < itemCount; i++) {
            System.out.print("구매할 상품 ID를 입력하세요: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            System.out.print("구매 수량을 입력하세요: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            productIds.add(productId);
            quantities.add(quantity);
        }
 		
 		
 		
 		try {
 		    connection.setAutoCommit(false);

 		    String query1 = "SELECT price, quantity "
 		            + "FROM product "
 		            + "WHERE product_id = ?";
 		    PreparedStatement statement1 = connection.prepareStatement(query1);

 		    String query2 = "UPDATE product SET quantity = quantity - ? WHERE product_id = ?";
 		    PreparedStatement statement2 = connection.prepareStatement(query2);

 		    String query3 = "UPDATE k_member "
 		            + "SET cash = cash - ? "
 		            + "WHERE id = ?";
 		    PreparedStatement statement3 = connection.prepareStatement(query3);

            System.out.println("\n========== 영수증 ==========");
 		    for (int i = 0; i < productIds.size(); i++) {
 		        int productId = productIds.get(i);
 		        int quantity = quantities.get(i);

 		        statement1.setInt(1, productId);
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

 		            System.out.println("구매 상품 ID: " + productId);
 		            System.out.println("구매 수량: " + quantity);
 		            System.out.println("총 가격: " + (price * quantity));
 		            System.out.println("구매 시간: " + currentTime);
 		            System.out.println("============================");

 		            statement2.setInt(1, quantity);
 		            statement2.setInt(2, productId);
 		            statement2.executeUpdate();
 		        } else {
 		            throw new Exception("상품을 찾을 수 없습니다.");
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
 				System.out.println("구매하기에 실패하셨습니다. 원하시는 상품의 재고를 확인해주세요.");
 			}
 		}finally {
 			if(connection != null) {
 				try {
 					connection.setAutoCommit(true);
 				} catch (SQLException e2) {}
 			}
 			System.out.println("보유 현금: " + getUserCash());
 			showMenu();
 		}
    }
    
    
    
    
    public void rechargeCash() {
        System.out.print("충전할 금액을 입력하세요: ");
        double cash = Double.parseDouble(scanner.nextLine());

        if (updateUserCash(getUserCash() + cash)) {
            System.out.println("현금을 충전하였습니다.");
        } else {
            System.out.println("현금 충전에 실패했습니다.");
        }
        showMenu();
    }



    private double getUserCash() {
        try {
        	String query = "SELECT cash FROM k_member WHERE id=?";
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


    private boolean updateUserCash(double cash) {
        try {
            String query = "UPDATE k_member"
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
    
    
    
    
    private void register() {
    	KMember kmember = new KMember();
        System.out.print("사용할 아이디를 입력하세요: ");
        kmember.id = scanner.nextLine();
        System.out.print("사용할 비밀번호를 입력하세요: ");
        kmember.password = scanner.nextLine();

        if (isIdAvailable(kmember.id)) {
            try {
            	String query = "" +
            			"INSERT INTO k_member (id, password, cash)"+
            			" VALUES (?, ?, ?)";
            	PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, kmember.getId());
                statement.setString(2, kmember.getPassword());
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
    
    private boolean isIdAvailable(String id) {
        try {
        	String query = "" +
        			"SELECT * FROM k_member"+
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
                            System.out.println("0. 돌아가기");
                            System.out.print("메뉴 선택: ");
                            int choice = scanner.nextInt();
                            scanner.nextLine(); // 버퍼 비우기

                            switch (choice) {
                                case 1:
                                    fillStock();
                                    break;
                                case 2:
                                    displayProducts();
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


    private void fillStock() {
        System.out.print("추가할 상품 ID를 입력하세요: ");
        int productId = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        System.out.print("추가할 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        try {
            String query = ""
            		+ "UPDATE product"
            		+ " SET quantity = quantity + ?"
            		+ " WHERE product_id = ?";
        	PreparedStatement statement= connection.prepareStatement(query);
        	statement.setInt(1, quantity);
        	statement.setInt(2, productId);
            int rowsUpdated = statement.executeUpdate();
            statement.close();
            if (rowsUpdated > 0) {
                System.out.println("재고를 추가하였습니다.");
            } else {
                System.out.println("상품 ID를 확인하세요.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}
