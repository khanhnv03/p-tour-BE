package com.ptit.tour.domain.deal.service;

import com.ptit.tour.domain.deal.dto.ApplyDealResponse;
import com.ptit.tour.domain.deal.dto.DealDto;
import com.ptit.tour.domain.deal.dto.SaveDealRequest;
import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface DealService {
    List<DealDto> getActivePublicDeals();
    Page<DealDto> findAll(Pageable pageable);
    Page<DealDto> searchAdmin(DealStatus status, String keyword, String dateState, Pageable pageable);
    DealDto getById(Long id);
    ApplyDealResponse applyPromoCode(String promoCode, BigDecimal subtotal);
    ApplyDealResponse findBestAutoApply(BigDecimal subtotal);
    DealDto create(SaveDealRequest request);
    DealDto update(Long id, SaveDealRequest request);
    void delete(Long id);
    Deal getEntityById(Long id);
}
