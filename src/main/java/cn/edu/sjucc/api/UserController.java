package cn.edu.sjucc.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.sjucc.UserExistException;
import cn.edu.sjucc.model.Result;
import cn.edu.sjucc.model.User;
import cn.edu.sjucc.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	public CacheManager cacheManager;
	
	@Autowired
	private UserService service;
	
	@PostMapping("/register")
	public Result register(@RequestBody User u) {
		logger.debug("即将注册用户，用户数据：" +u);
		Result result = new Result();
		User saved = null;
		try {
			saved = service.createUser(u);
			result.setStatus(Result.OK);
			result.setMessage("注册成功");
			result.setDate(saved);
		} catch (UserExistException e) {
			logger.error("注册用户已存在。", e);
			result.setStatus(Result.ERROR);
			result.setMessage("用户已存在!");
		}
		
		return result;
	}
	
	@GetMapping("/login/{username}/{password}")
	public Result login(@PathVariable String username, 
			@PathVariable String password) {
		Result result = new Result();
		boolean status = service.checkUser(username, password);
		if(status) {
			result.setStatus(Result.OK);
			//把用户存入缓存
			Cache cache = cacheManager.getCache(User.CACHE_NAME);
			cache.put("current_user", username);
		}else {
			result.setStatus(Result.ERROR);
		}
		return result;
	}
	
	
}
