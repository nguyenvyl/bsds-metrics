CREATE DEFINER=`admin`@`%` PROCEDURE `calculate_user_stats`(IN dayNum int)
BEGIN
    SET @userIndex = 1;
    WHILE @userIndex < 40001 DO
		SET @inputTable = concat("rfid_data_day_", dayNum);
        SET @outputTable = concat("user_data_day_", dayNum);
		SET @sql_text = concat(
        "REPLACE ", @outputTable, " (skierID, totalLifts, totalVert, dayNum) ",
		"SELECT ", @userIndex, " as skierID, SUM(CASE WHEN skierID = ", @userIndex, " THEN 1 ELSE 0 END) AS lifts, ",
		" SUM(CASE WHEN skierID = ", @userIndex, " AND liftID < 11  THEN 200 ELSE 0 END) ",
		" + SUM(CASE WHEN skierID = ", @userIndex, " AND liftID > 10 AND liftID < 21 THEN 300 ELSE 0 END) ",
		" + SUM(CASE WHEN skierID = ", @userIndex, " AND liftID > 20 AND liftID < 31 THEN 400 ELSE 0 END) ",
		" + SUM(CASE WHEN skierID = ", @userIndex, " AND liftID > 30 AND liftID < 41 THEN 500 ELSE 0 END) AS vert, ",
		 dayNum, " as dayNum FROM ", @inputTable);
        PREPARE stmt FROM @sql_text;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		SET @userIndex = @userIndex + 1;
	END WHILE;
END