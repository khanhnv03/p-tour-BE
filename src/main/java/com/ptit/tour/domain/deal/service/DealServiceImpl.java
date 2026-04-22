package com.ptit.tour.domain.deal.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.deal.dto.ApplyDealResponse;
import com.ptit.tour.domain.deal.dto.DealDto;
import com.ptit.tour.domain.deal.dto.SaveDealRequest;
import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;

    @Override
    public List<DealDto> getActivePublicDeals() {
        return dealRepository.findByStatus(DealStatus.ACTIVE, Pageable.unpaged())
            .map(DealDto::from).toList();
    }

    @Override
    public Page<DealDto> findAll(Pageable pageable) {
        return dealRepository.findAll(pageable).map(DealDto::from);
    }

    @Override
    public DealDto getById(Long id) {
        return DealDto.from(getEntityById(id));
    }

    @Override
    public ApplyDealResponse applyPromoCode(String promoCode, BigDecimal subtotal) {
        Deal deal = dealRepository.findByPromoCode(promoCode)
            .orElseThrow(() -> new BusinessException("Mã khuyến mãi không hợp lệ"));

        validateDeal(deal, subtotal);

        BigDecimal discount = deal.computeDiscount(subtotal);
        return new ApplyDealResponse(deal.getId(), promoCode, discount,
            "Áp dụng mã thành công, giảm " + discount + " VNĐ");
    }

    @Override
    public ApplyDealResponse findBestAutoApply(BigDecimal subtotal) {
        List<Deal> deals = dealRepository.findBestAutoApply(LocalDate.now(), subtotal);
        if (deals.isEmpty()) return null;
        Deal best = deals.get(0);
        BigDecimal discount = best.computeDiscount(subtotal);
        return new ApplyDealResponse(best.getId(), best.getPromoCode(), discount,
            "Tự động áp dụng ưu đãi: " + best.getTitle());
    }

    @Override
    @Transactional
    public DealDto create(SaveDealRequest req) {
        Deal deal = Deal.builder()
            .title(req.title()).description(req.description())
            .campaignImageUrl(req.campaignImageUrl()).badgeText(req.badgeText())
            .category(req.category()).discountType(req.discountType())
            .discountValue(req.discountValue()).promoCode(req.promoCode())
            .displayMode(req.displayMode())
            .minOrderValue(req.minOrderValue() != null ? req.minOrderValue() : BigDecimal.ZERO)
            .maxDiscountAmount(req.maxDiscountAmount())
            .usageLimit(req.usageLimit()).validFrom(req.validFrom())
            .validTo(req.validTo()).status(req.status()).build();
        return DealDto.from(dealRepository.save(deal));
    }

    @Override
    @Transactional
    public DealDto update(Long id, SaveDealRequest req) {
        Deal deal = getEntityById(id);
        deal.setTitle(req.title()); deal.setDescription(req.description());
        deal.setCampaignImageUrl(req.campaignImageUrl()); deal.setBadgeText(req.badgeText());
        deal.setCategory(req.category()); deal.setDiscountType(req.discountType());
        deal.setDiscountValue(req.discountValue()); deal.setPromoCode(req.promoCode());
        deal.setDisplayMode(req.displayMode());
        if (req.minOrderValue() != null) deal.setMinOrderValue(req.minOrderValue());
        deal.setMaxDiscountAmount(req.maxDiscountAmount());
        deal.setUsageLimit(req.usageLimit());
        deal.setValidFrom(req.validFrom()); deal.setValidTo(req.validTo());
        deal.setStatus(req.status());
        return DealDto.from(dealRepository.save(deal));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        dealRepository.delete(getEntityById(id));
    }

    @Override
    public Deal getEntityById(Long id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", id));
    }

    private void validateDeal(Deal deal, BigDecimal subtotal) {
        LocalDate today = LocalDate.now();
        if (deal.getStatus() != DealStatus.ACTIVE)
            throw new BusinessException("Deal không còn hoạt động");
        if (today.isBefore(deal.getValidFrom()) || today.isAfter(deal.getValidTo()))
            throw new BusinessException("Deal đã hết hạn");
        if (subtotal.compareTo(deal.getMinOrderValue()) < 0)
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu " + deal.getMinOrderValue() + " VNĐ");
        if (deal.getUsageLimit() != null && deal.getUsageCount() >= deal.getUsageLimit())
            throw new BusinessException("Deal đã hết lượt sử dụng", HttpStatus.GONE);
    }
}
