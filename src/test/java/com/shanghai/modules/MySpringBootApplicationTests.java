package com.shanghai.modules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.util.ByteSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;
import com.shanghai.common.shiro.ShiroRedisSessionDao;
import com.shanghai.common.utils.PageInfo;
import com.shanghai.common.utils.UserUtil;
import com.shanghai.common.utils.excel.ExportExcel;
import com.shanghai.modules.sys.dao.MenuDao;
import com.shanghai.modules.sys.dao.RoleDao;
import com.shanghai.modules.sys.dao.UserDao;
import com.shanghai.modules.sys.entity.Menu;
import com.shanghai.modules.sys.entity.Role;
import com.shanghai.modules.sys.entity.User;
import com.shanghai.modules.sys.service.RoleService;
import com.shanghai.modules.sys.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MySpringBootApplicationTests {
	
	@Autowired
	private RoleDao roleDao;

	@Autowired
	private MenuDao menuDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	RedisTemplate<Object, Object> redisTemplate;
	
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	
	@Autowired
	ShiroRedisSessionDao shiroRedisSessionDao;
	
	@Test
	public void test() {
		try {
			userService.deleteUserById(6);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test1() {
		User user = new User();
		PageInfo<User> users = userService.findUserByPage(1, 4,user);
		String html = users.getHtml();
		System.out.println(html);
	}
	
	/**
	 * shiro对密码进行md5盐值加密
	 */
	@Test
	public void test2() {
		String hashAlgorithmName = "MD5";
		Object credentials = "123456";
		Object salt = ByteSource.Util.bytes("admin");
		int hashIterations = 1024;
		
		Object result = new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
		System.out.println(result);
	}
	
	@Test
	public void test3() {
		User user = userDao.getUserByName("yejiarong");
		System.out.println(user);
		redisTemplate.opsForValue().set("hi", user);
	}
	
	@Test
	public void test4() {
		User user = new User();
		user.setPassword("123456");
		user.setPhone("13615248452");
		for (int i = 1; i < 1000000; i++) {
			String name = "yejiarong" + i ;
			System.out.println(name);
			user.setUsername(name);
			String newPassword = UserUtil.encryptPassword(user.getUsername(), user.getPassword());
			user.setPassword(newPassword);
			userDao.insert(user);
		}
	}
	
	@Test
	public void test5() throws IOException {
		userService.queryByLoginNameAndPhoneNo(0, 3, "he", "151");
	}
	
	
	@Test
	public void test7() {
		List<Role> roles = roleDao.findList(new Role());
		System.out.println(roles.size());
	}
	
	@Test
	public void test8() {
		Collection<Session> sessions = shiroRedisSessionDao.getActiveSessions();
		System.out.println(sessions.size());
	}
	
	@Test
	public void test9() {
		Role role = new Role();
		role.setId(1);
		role = roleDao.get(role);
		System.out.println(role);
	}
	
	
	
	@Test
	public void test11() {
		List<Role> list = roleDao.findUserAllRole(2);
		System.out.println(list.size());
	}
	
	@Test
	public void test12() {
		Role role = roleService.getRoleById(5);
		System.out.println(role);
	}
	
	@Test
	public void test13() {
		Menu menu = menuDao.get(new Menu(2));
		System.out.println(menu);
	}
	
	@Test
	public void test14() throws FileNotFoundException, IOException {
		List<String> headerList = Lists.newArrayList();
		for (int i = 1; i <= 10; i++) {
			headerList.add("表头"+i);
		}
		
		List<String> dataRowList = Lists.newArrayList();
		for (int i = 1; i <= headerList.size(); i++) {
			dataRowList.add("数据"+i);
		}
		
		List<List<String>> dataList = Lists.newArrayList();
		for (int i = 1; i <=1000000; i++) {
			dataList.add(dataRowList);
		}

		ExportExcel ee = new ExportExcel("表格标题", headerList);
		
		for (int i = 0; i < dataList.size(); i++) {
			Row row = ee.addRow();
			for (int j = 0; j < dataList.get(i).size(); j++) {
				ee.addCell(row, j, dataList.get(i).get(j));
			}
		}
		
		ee.writeFile("E:/export.xlsx");

		ee.dispose();
		
	}
	
	@Test
	public void Test15() throws ParseException {
		Date aData = DateUtils.parseDate("2014-04-05", "yyyy-MM-dd");
		System.out.println(aData.toString());
	}
	
}
