package com.bilibili.admin.Controller;

import com.bilibili.entity.po.CategoryInfo;
import com.bilibili.entity.query.CategoryInfoQuery;
import com.bilibili.entity.vo.ResponseVO;
import com.bilibili.service.CategoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController extends ABaseController{
    @Autowired
    private CategoryInfoService categoryInfoService;

    /**
     * 获取所有分类，并根据sort值升序排列
     * @param categoryInfoQuery
     * @return
     */
    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory(CategoryInfoQuery categoryInfoQuery){
        categoryInfoQuery.setOrderBy("sort asc");
        //以树型展示
        categoryInfoQuery.setConverToTree(true);
        List<CategoryInfo> list = categoryInfoService.findListByParam(categoryInfoQuery);
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(@NotEmpty Integer pCategoryId,
                                   Integer categoryId,
                                   @NotEmpty String categoryCode,
                                   @NotEmpty String categoryName,
                                   String icon,
                                   String background){
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setCategoryCode(categoryCode);
        categoryInfo.setpCategoryId(pCategoryId);
        categoryInfo.setCategoryId(categoryId);
        categoryInfo.setIcon(icon);
        categoryInfo.setBackground(background);
        categoryInfo.setCategoryName(categoryName);
        categoryInfoService.saveCategory(categoryInfo);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delCategory")
    public ResponseVO delCategory(@NotEmpty Integer categoryId){
        categoryInfoService.delCategory(categoryId);
        return getSuccessResponseVO(null);
    }
}
