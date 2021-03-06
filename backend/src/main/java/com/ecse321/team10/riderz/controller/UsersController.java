package com.ecse321.team10.riderz.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecse321.team10.riderz.dto.DriverPerformanceDto;
import com.ecse321.team10.riderz.dto.UserDto;
import com.ecse321.team10.riderz.model.DriverPerformance;
import com.ecse321.team10.riderz.model.User;
import com.ecse321.team10.riderz.sql.MySQLJDBC;

@RestController
@RequestMapping("users")
public class UsersController {
	
	@Autowired
	private MySQLJDBC sql;
	
	@Autowired
	private ModelMapper modelMapper;
	
	private UserDto convertToDto(User user) {
		return modelMapper.map(user, UserDto.class);
	}
	
	private DriverPerformanceDto convertToDto(DriverPerformance dp) {
		return modelMapper.map(dp,  DriverPerformanceDto.class);
	}

	private static final Logger logger = LogManager.getLogger(UsersController.class);

	/**
	 * Handles the /users/addUser endpoint and creates a new user in the DB
	 * @param username - String
	 * @param password - String
	 * @param email - String
	 * @param phone - String
	 * @param firstName - String
	 * @param lastName - String
	 * @return UserDto object if successful otherwise null
	 */
	@PostMapping("addUser")
	public UserDto addUser(	@RequestParam String username,
							@RequestParam String password,
							@RequestParam String email,
							@RequestParam String phone,
							@RequestParam String firstName,
							@RequestParam String lastName) {
		User user = new User(username, password, email, phone, firstName, lastName);
		if(sql.connect() && sql.insertUser(user)) {
			sql.closeConnection();
			return convertToDto(user);
		}else{
			sql.closeConnection();
			return null;
		}

	}

	/**
	 * Handles the /users/getUser endpoint, retrieves user based on username
	 * @param username - String
	 * @return UserDto if successful, otherwise null
	 */
	@GetMapping("getUser")
	public UserDto getUser(	@RequestParam String username) {
		if(sql.connect() && sql.verifyAuthentication(username)){
			User user = sql.getUserByUsername(username);
			sql.closeConnection();
			return convertToDto(user);
		}
		sql.closeConnection();
		return null;
	}

	/**
	 * Handles the /users/getAllUsers endpoint, retrieves all db users
	 * @param username - String
	 * @return List<UserDto>, list of UserDto objects otherwise null
	 */
	@GetMapping("getAllUsers")
	public List<UserDto> getAllUser(@RequestParam String username) {
		if(sql.connect() && sql.verifyAuthentication(username)){
			List<UserDto> users = new ArrayList<UserDto>();
			for(User user : sql.getAllUsers()){
				users.add(convertToDto(user));
			}
			sql.closeConnection();
			return users;
		}
		return null;
	}

	/**
	 * Handles /users/deleteUser endpoint, receives username and delete
	 * user from DB if verified password is received
	 * @param username - String
	 * @param password - String
	 * @return Boolean True if successful deletion, otherwise False
	 */
	//Note: Implemented basic authentication
	//todo is to better security
	@DeleteMapping("deleteUser")
	public boolean deleteUser(@RequestParam String username, @RequestParam String password){
		if(sql.connect() && sql.verifyLogin(username,password)){
			sql.deleteUser(username);
			sql.closeConnection();
			return true;
		}
		return false;
	}

	/**
	 * Handles the /users/getPhone endpoint, retrieves user phone number
	 * @param username - String
	 * @return String of the user phone number, otherwise null
	 */
	@GetMapping("getPhone")
	public String getPhone( @RequestParam String username ){
		if (sql.connect() && sql.verifyAuthentication(username)) {
			String phone = sql.getPhone(username);
			sql.closeConnection();
			return phone;
		}
		return null;
	}

	/**
	 * Handles the /users/setPhone endpoint, changes user phone number
	 * @param username - String
	 * @param phone - String
	 * @return Boolean true if number was changed, otherwise false
	 */
	@PutMapping("setPhone")
	public boolean setPhone( @RequestParam String username,
							 @RequestParam String phone){
		if (sql.connect() && sql.verifyAuthentication(username)) {
			boolean numberChanged = sql.setPhone(username, phone);
			sql.closeConnection();
			return numberChanged;
		}
		return false;
	}

	/**
	 * Handles the users/getEmail endpoint, retrieves userEmail
	 * @param username - String
	 * @return String if email exists, otherwise null
	 */
	@GetMapping("getEmail")
	public String getEmail( @RequestParam String username ){
		if (sql.connect()) {
			String email = sql.getEmail(username);
			sql.closeConnection();
			return email;
		}
		return null;
	}


	/**
	 * Handles the users/setEmail endpoint, sets a new email to user
	 * @param username - String
	 * @param email - String
	 * @return Boolean true if email has been changed, otherwise false
	 */
	@PutMapping("setEmail")
	public boolean setEmail( @RequestParam String username,
							 @RequestParam String email){
		if (sql.connect() && sql.verifyAuthentication(username)) {
			boolean emailChanged = sql.setEmail(username,email);
			sql.closeConnection();
			return emailChanged;
		}
		return false;
	}

	/**
	 * Handles the users/setPassword endpoint, verifies the user credentials and then changes password
	 * @param username - String
	 * @param password - String
	 * @param newPassword - String
	 * @return Boolean true if password was changed, otherwise false
	 */
	//Successfully changes password but can implement email verification later
	@PutMapping("setPassword")
	public boolean setPassword(@RequestParam String username,
							   @RequestParam String newPassword) {
		if (sql.connect()) {
			boolean changedPass = sql.setPassword(username, newPassword);
			sql.closeConnection();
			return changedPass;
		}
		sql.closeConnection();
		return false;
	}

    /**
     * Obtains the number of trips offered by drivers in descending order.
     * @param startingTime	-	A String representing a MySQL timestamp
     * @param endingTime	-	A String representing a MySQL timestamp
     * @return A list of drivers and the number of trips they are offering.
     */
    @RequestMapping(value = "getDriverPerformance", method = RequestMethod.GET)
    public List<DriverPerformanceDto> getDriverPerformance( @RequestParam String startingTime,
    														@RequestParam String endingTime) {
    	if (sql.connect()) {
    		List<DriverPerformanceDto> dps = new ArrayList<>();
    		Timestamp startTime = sql.convertStringToTimestamp(startingTime + ".000");
    		Timestamp endTime = sql.convertStringToTimestamp(endingTime + ".000");
    		for (DriverPerformance dp : sql.getDriverPerformance(startTime, endTime)) {
    			dps.add(convertToDto(dp));
    		}
    		sql.closeConnection();
    		logger.info("Successfully obtained driver performance");
    		return dps;
    	}
    	logger.error("Failed to establish communication with SQL database");
    	return null;
    }

    /**
     * Obtains the number of trips reserved by passengers in descending order.
     * @param startingTime	-	A String representing a MySQL timestamp
     * @param endingTime	-	A String representing a MySQL timestamp
     * @return A list of users and the number of trips they have reserved.
     */
    @RequestMapping(value = "getUserPerformance", method = RequestMethod.GET)
    public List<DriverPerformanceDto> getUserPerformance( @RequestParam String startingTime,
    													  @RequestParam String endingTime) {
    	if (sql.connect()) {
    		List<DriverPerformanceDto> ups = new ArrayList<>();
    		Timestamp startTime = sql.convertStringToTimestamp(startingTime + ".000");
    		Timestamp endTime = sql.convertStringToTimestamp(endingTime + ".000");
    		for (DriverPerformance dp : sql.getUserPerformance(startTime, endTime)) {
    			ups.add(convertToDto(dp));
    		}
    		sql.closeConnection();
    		logger.info("Successfully obtained driver performance");
    		return ups;
    	}
    	logger.error("Failed to establish communication with SQL database");
    	return null;
    }
}
