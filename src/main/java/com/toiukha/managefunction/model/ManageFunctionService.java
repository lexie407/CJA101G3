package com.toiukha.managefunction.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManageFunctionService {

	@Autowired
	private ManageFunctionRepository repository;

	// 取出所有管理功能
	public List<ManageFunctionVO> getAll() {
		return repository.findAll();
	}

	// 用 ID 找單筆功能
	public Optional<ManageFunctionVO> findById(Integer id) {
		return repository.findById(id);
	}

	// 新增
	public ManageFunctionVO create(ManageFunctionVO vo) {
		return repository.save(vo);
	}

	// 更新
	public ManageFunctionVO update(ManageFunctionVO vo) {
		return repository.save(vo);
	}

}
