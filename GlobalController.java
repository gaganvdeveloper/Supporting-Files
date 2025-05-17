
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JWTUtils;
import com.example.demo.dto.AuthUser;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.responsestructure.ResponseStructure;
import com.example.demo.util.Role;

@RestController
public class GlobalController {
	
	@Autowired
	private JWTUtils jwtUtils;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping("/save")
	public ResponseEntity<?> saveUser(@RequestBody User user){
		if(userRepository.existsByEmail(user.getEmail()))
			throw new RuntimeException("Email Already Exist...");
		user.setUsername(passwordEncoder.encode(user.getUsername()));
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if(user.getRole()==null)
			user.setRole(Role.USER);
		user = userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value()).message("User Saved Successfully").token("TOKEN").role(user.getRole()).body(user).build());
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthUser auth){
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));
		var user = userRepository.findByEmail(auth.getEmail()).orElseThrow(()->new RuntimeException("Invalid Email or Password"));
		var token = jwtUtils.generateToken(user);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value()).message("Login Successfull").body(user).token(token).build());
	}
	
}
