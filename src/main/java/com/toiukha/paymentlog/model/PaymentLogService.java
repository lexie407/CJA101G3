package com.toiukha.paymentlog.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.sentitem.model.SentItemVO;

@Service("paymentLogService")
public class PaymentLogService {

	@Autowired
	PaymentLogRepository repository;

	public void addPaymentLog(PaymentLogVO paymentlogVO) {
		repository.save(paymentlogVO);
	}

	public void updatePaymentLog(PaymentLogVO paymentlogVO) {
		repository.save(paymentlogVO);
	}

	public PaymentLogVO getOnePaymentLog(Integer payId) {
		Optional<PaymentLogVO> optional = repository.findById(payId);
		return optional.orElse(null);
	}

	public List<PaymentLogVO> getAll() {
		return repository.findAll();
	}

	public PaymentLogVO getByOrdId(Integer ordId) {
		List<PaymentLogVO> all = getAll();
		for (PaymentLogVO vo : all) {
			if (vo.getOrdId() != null && vo.getOrdId().equals(ordId)) {
				return vo;
			}
		}
		return null;
	}
}
