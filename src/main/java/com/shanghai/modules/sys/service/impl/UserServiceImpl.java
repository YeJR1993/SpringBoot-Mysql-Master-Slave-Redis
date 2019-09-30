package com.shanghai.modules.sys.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.shanghai.common.utils.PageInfo;
import com.shanghai.common.utils.UserUtil;
import com.shanghai.common.utils.constant.EsConstants;
import com.shanghai.modules.sys.dao.UserDao;
import com.shanghai.modules.sys.entity.User;
import com.shanghai.modules.sys.service.UserService;

import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

/**
 * @author: YeJR
 * @version: 2018年4月28日 上午10:19:22
 * 
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private JestClient jestClient;

	@Override
	public User getByLoginName(String loginName) {
		return userDao.getUserByName(loginName);
	}
	
	@Override
	public User getUserById(Integer id) {
		User user = new User(id);
		return userDao.get(user);
	}
	
	@Override
	public List<User> findUsers(User user) {
		return userDao.findList(user);
	}
	
	@Override
	public PageInfo<User> findUserByPage(int pageNo, int pageSize, User user) {
		PageHelper.startPage(pageNo, pageSize);
		return new PageInfo<>(userDao.findList(user));
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Integer saveUser(User user) throws IOException {
		String newPassword = UserUtil.encryptPassword(user.getUsername(), user.getPassword());
		user.setPassword(newPassword);
		// 保存到db中
		Integer result = userDao.insert(user);
		if (user.getRoleIds() != null && user.getRoleIds().size() > 0) {
			//插入新的用户角色关联关系
			userDao.insertUserRole(user);
		}
		// 索引（保存）到ES中
		Index index = new Index.Builder(user).index(EsConstants.INDEX_SYSTEM).type(EsConstants.TYPE_USER).build();
		jestClient.execute(index);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Integer updateUser(User user) throws IOException {
		if (StringUtils.isNotBlank(user.getPassword())) {
			String newPassword = UserUtil.encryptPassword(user.getUsername(), user.getPassword());
			user.setPassword(newPassword);
		}
		Integer result = userDao.update(user);
		//删除原有的用户角色关联关系
		userDao.deleteUserRole(user);
		if (user.getRoleIds() != null && user.getRoleIds().size() > 0) {
			//插入新的用户角色关联关系
			userDao.insertUserRole(user);
		}
		// 索引（保存）到ES中, 有相同的id会更新覆盖
		Index index = new Index.Builder(user).index(EsConstants.INDEX_SYSTEM).type(EsConstants.TYPE_USER).build();
		jestClient.execute(index);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void deleteUserById(Integer id) throws IOException {
		User user = new User(id);
		userDao.delete(user);
		//删除用户角色--角色关系
		userDao.deleteUserRole(user);
		// 删除ES中的文档
		Delete builder = new Delete.Builder(id.toString()).index(EsConstants.INDEX_SYSTEM).type(EsConstants.TYPE_USER).build();
		jestClient.execute(builder);
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void deleteUserByIds(Integer[] ids) throws IOException {
		if (ids != null) {
			for (Integer id : ids) {
				deleteUserById(id);
			}
		}
		
	}
	
	@Override
	public boolean verifyUsername(String username, String oldUsername) {
		if (StringUtils.isNotBlank(username)) {
			if (username.equals(oldUsername)) {
				return true;
			}
			User user = userDao.getUserByName(username);
			if (user == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<User> queryForLoginName(int pageNo, int pageSize, String userName) throws IOException {
		List<User> users = new ArrayList<User>();
		// 查询
		QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("username", "*" + userName + "*");
		// 高亮
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		highlightBuilder.field("username");
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.highlighter(highlightBuilder);
		searchSourceBuilder.size(pageSize);
		searchSourceBuilder.from(pageNo);
		String query = searchSourceBuilder.toString();

		Search search = new Search.Builder(query).addIndex(EsConstants.INDEX_SYSTEM).addType(EsConstants.TYPE_USER).build();
		SearchResult result = jestClient.execute(search);
		
		List<Hit<User, Void>> hits = result.getHits(User.class);
		for (Hit<User, Void> hit : hits) {
			User user = hit.source;
			users.add(user);
		}
		return users;
	}

	@Override
	public List<User> queryByLoginNameAndPhoneNo(int pageNo, int pageSize, String userName, String phoneNo)
			throws IOException {
		List<User> users = new ArrayList<User>();
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.must(QueryBuilders.wildcardQuery("username", "*" + userName + "*"));
		boolQueryBuilder.must(QueryBuilders.wildcardQuery("phone", "*" + phoneNo + "*"));
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.size(pageSize);
		searchSourceBuilder.from(pageNo);
		String query = searchSourceBuilder.toString();

		Search search = new Search.Builder(query).addIndex(EsConstants.INDEX_SYSTEM).addType(EsConstants.TYPE_USER).build();
		SearchResult result = jestClient.execute(search);
		
		List<Hit<User, Void>> hits = result.getHits(User.class);
		for (Hit<User, Void> hit : hits) {
			User user = hit.source;
			users.add(user);
		}
		return users;
	}


}
