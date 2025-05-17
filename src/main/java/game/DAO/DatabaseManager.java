package game.DAO;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/player?useSSL=false&serverTimezone=UTC";
    static Connection connexion = null;
    static String user="root";
    static String password="";

    private static Connection seConnecter(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connexion = DriverManager.getConnection(DB_URL, user, password);
        } catch (Exception e){
            e.printStackTrace();
        }
        return connexion;
    }

    public static void seDeconnecter(){
        try{
            connexion.close();
        }catch(SQLException e){
        }
    }

    public boolean playerExists(String username) {
        String sql = "SELECT * FROM joueur WHERE nom = '"+username+"'";
        try{
            Statement stm = seConnecter().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createPlayer(String username, int score) {
        String sql = "INSERT INTO joueur VALUES ('"+username+"', "+score+");";
        try{
            Statement stm = seConnecter().createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHighscore(String username) {
        String sql = "SELECT record FROM joueur WHERE nom = '"+username+"';";
        try{
            Statement stm = seConnecter().createStatement();
            ResultSet rs = stm.executeQuery(sql);
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void updateHighscore(String username, int score){
        String sql = "UPDATE joueur SET record = "+score+" WHERE nom = '"+username+"';";
        try{
            Statement stm = seConnecter().createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}