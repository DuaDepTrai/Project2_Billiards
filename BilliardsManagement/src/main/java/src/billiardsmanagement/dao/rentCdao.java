// Integrated function from finishEachCueRmental
public static boolean endAllCueRentals(int orderId, ObservableList<RntCue> list) {
    String updateRntCueQuery = "UPDATE rent_cues SET end_time = ?, timeplay = ?, subtotal = ?, net_total = ?, status = ? WHERE rent_cue_id = ?";
    Connection conn = DatabaseConnection.getConnection();
    PreparedStatement pstmt = null;

    try {
        if (conn == null) {
            throw new SQLException("Database connection is null");
        }
        
