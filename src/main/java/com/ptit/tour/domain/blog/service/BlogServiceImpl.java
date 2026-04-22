package com.ptit.tour.domain.blog.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.blog.dto.BlogPostDto;
import com.ptit.tour.domain.blog.dto.BlogPostSummaryDto;
import com.ptit.tour.domain.blog.dto.SaveBlogPostRequest;
import com.ptit.tour.domain.blog.entity.BlogBlock;
import com.ptit.tour.domain.blog.entity.BlogPost;
import com.ptit.tour.domain.blog.enums.BlogStatus;
import com.ptit.tour.domain.blog.repository.BlogPostRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    @Override
    public Page<BlogPostSummaryDto> listPublished(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return blogPostRepository.findByTitleContainingIgnoreCaseAndStatus(
                keyword, BlogStatus.PUBLISHED, pageable).map(BlogPostSummaryDto::from);
        }
        return blogPostRepository.findByStatus(BlogStatus.PUBLISHED, pageable).map(BlogPostSummaryDto::from);
    }

    @Override
    public BlogPostDto getBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
            .map(BlogPostDto::from)
            .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "slug", slug));
    }

    @Override
    public BlogPostDto getById(Long id) {
        return BlogPostDto.from(findEntityById(id));
    }

    @Override
    public Page<BlogPostSummaryDto> findAll(Pageable pageable) {
        return blogPostRepository.findAll(pageable).map(BlogPostSummaryDto::from);
    }

    @Override
    @Transactional
    public BlogPostDto create(Long authorId, SaveBlogPostRequest req) {
        String slug = SlugUtils.toSlug(req.title());
        if (blogPostRepository.existsBySlug(slug)) {
            throw new BusinessException("Tiêu đề bài viết đã tồn tại");
        }
        var author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        BlogPost post = BlogPost.builder()
            .author(author).title(req.title()).slug(slug)
            .coverImageUrl(req.coverImageUrl()).excerpt(req.excerpt())
            .status(req.status()).scheduledAt(req.scheduledAt()).build();

        if (req.status() == BlogStatus.PUBLISHED) post.setPublishedAt(Instant.now());

        applyBlocks(post, req);
        return BlogPostDto.from(blogPostRepository.save(post));
    }

    @Override
    @Transactional
    public BlogPostDto update(Long id, SaveBlogPostRequest req) {
        BlogPost post = findEntityById(id);
        post.setTitle(req.title());
        post.setCoverImageUrl(req.coverImageUrl());
        post.setExcerpt(req.excerpt());
        if (req.status() == BlogStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
        post.setStatus(req.status());
        post.setScheduledAt(req.scheduledAt());
        post.getBlocks().clear();
        applyBlocks(post, req);
        return BlogPostDto.from(blogPostRepository.save(post));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        blogPostRepository.delete(findEntityById(id));
    }

    @Override
    @Scheduled(fixedDelay = 60_000) // every minute
    @Transactional
    public void publishScheduled() {
        List<BlogPost> due = blogPostRepository.findDueScheduledPosts(Instant.now());
        due.forEach(p -> {
            p.setStatus(BlogStatus.PUBLISHED);
            p.setPublishedAt(Instant.now());
            log.info("Auto-published blog post id={} slug={}", p.getId(), p.getSlug());
        });
    }

    private BlogPost findEntityById(Long id) {
        return blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));
    }

    private void applyBlocks(BlogPost post, SaveBlogPostRequest req) {
        if (req.blocks() == null) return;
        List<BlogBlock> blocks = req.blocks().stream()
            .map(b -> BlogBlock.builder()
                .blogPost(post).blockType(b.blockType()).content(b.content())
                .imageUrl(b.imageUrl()).sortOrder(b.sortOrder()).build())
            .toList();
        post.getBlocks().addAll(blocks);
    }
}
