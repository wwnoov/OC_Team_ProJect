//데이터베이스 연결을 위한 클래스입니다.
import java.sql.*;


public class DBConnector {
    Connection connection;
    private PreparedStatement statement;

    public DBConnector() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			
			// 연결하기
			connection = DriverManager.getConnection(
					"jdbc:mariadb://127.0.0.1:3306/tp_test",
					"root",
					"12345"
					);
			// System.out.println("연결확인");				//연결확인용 콘솔출력
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("DB 연결에 문제가 생겨 프로그램을 종료합니다.");
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("DB 연결에 문제가 생겨 프로그램을 종료합니다.");
			System.exit(0);
		}
	}
    
    public void exit() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}