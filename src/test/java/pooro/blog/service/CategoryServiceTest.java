package pooro.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pooro.blog.domain.Category;
import pooro.blog.error.ErrorCode;
import pooro.blog.exception.category.CategoryDuplicateException;
import pooro.blog.repository.CategoryRepository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Spy private FileService fileService;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    void 카테고리_생성() throws IOException {
        //given
        Long categoryId = 1L;
        String name = "test";
        Category category = Category.createCategory(categoryId, name);

        given(categoryRepository.save(any())).willReturn(category);
        given(categoryRepository.findOne(categoryId)).willReturn(Optional.ofNullable(category));

        //when
        Long saveId = categoryService.create(name);

        //then
        Category findCategory = categoryRepository.findOne(saveId).get();
        File folderInTemp = new File("posts/temp/" + findCategory.getName());
        File folderInPublic = new File("posts/public/" + findCategory.getName());

        assertEquals(category.getId(), findCategory.getId(), "저장된 ID 값이 같아야 합니다.");
        assertEquals(category.getName(), findCategory.getName(), "저장된 카테고리 이름이 같아야 합니다.");
        assertTrue(folderInTemp.exists(), "temp 폴더에 카테고리 폴더가 생성되어야 합니다.");
        assertTrue(folderInPublic.exists(), "public 폴더에 카테고리 폴더가 생성되어야 합니다.");

        //after
        if (folderInTemp.exists()) folderInTemp.delete();
        if (folderInPublic.exists()) folderInPublic.delete();
    }

    @Test
    void 카테고리_생성_중복_예외() {
        //given
        String name = "test";
        Category category = Category.createCategory("test");
        given(categoryRepository.findByName(name)).willReturn(Optional.ofNullable(category));

        //when
        CategoryDuplicateException thrown =
                assertThrows(CategoryDuplicateException.class, () -> categoryService.create(name));

        //then
        assertEquals(ErrorCode.CATEGORY_DUPLICATE, thrown.getErrorCode(), "카테고리 중복 예외를 발생시켜야 합니다.");
    }
}