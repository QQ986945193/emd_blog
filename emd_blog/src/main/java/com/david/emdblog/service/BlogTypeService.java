package com.david.emdblog.service;

import java.util.List;
import java.util.Map;

import com.david.emdblog.entity.BlogType;

/**
 * 文章,日志，文章类型
 * 
 * @Author ：程序员小冰
 * @新浪微博 ：http://weibo.com/mcxiaobing
 * @GitHub: https://github.com/QQ986945193
 */
public interface BlogTypeService {

	/**
	 * 查询所有文章类别以及对应的数量
	 */
	public List<BlogType> countList();

	/**
	 * 获取到分类数据
	 */
	public List<BlogType> list(Map<String, Object> map);
	/**
	 * 获取到分类数量
	 */
	public Long getTotal(Map<String, Object> map);
	/**
	 * 添加分类
	 */
	public int add(BlogType blogType);
	/**
	 * 修改分类
	 */
	public int update(BlogType blogType);
	/**
	 * 删除分类
	 */
	public void delete(int id);

}
