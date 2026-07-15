import type { AppRouteRecordRaw } from '/@/router/types';
import { t } from '/@/hooks/web/useI18n';
import { REDIRECT_NAME, LAYOUT, EXCEPTION_COMPONENT, PAGE_NOT_FOUND_NAME, PAGE_NOT_FOUND_NAME_404 } from '/@/router/constant';

// 404 on a page
export const PAGE_NOT_FOUND_ROUTE: AppRouteRecordRaw = {
  path: '/:path(.*)*',
 
  name: PAGE_NOT_FOUND_NAME,
  component: LAYOUT,
  meta: {
    title: 'ErrorPage',
    hideBreadcrumb: true,
    hideMenu: true,
  },
  children: [
    {
      path: '/:path(.*)*',
      // 代码逻辑说明: 【issues/7500】vue-router4.5.0版本路由name:PageNotFound同名导致登录进不去
      name: PAGE_NOT_FOUND_NAME_404,
      component: EXCEPTION_COMPONENT,
      meta: {
        title: 'ErrorPage',
        hideBreadcrumb: true,
        hideMenu: true,
      },
    },
  ],
};

export const REDIRECT_ROUTE: AppRouteRecordRaw = {
  path: '/redirect',
  component: LAYOUT,
  name: 'RedirectTo',
  meta: {
    title: REDIRECT_NAME,
    hideBreadcrumb: true,
    hideMenu: true,
  },
  children: [
    {
      path: '/redirect/:path(.*)',
      name: REDIRECT_NAME,
      component: () => import('/@/views/sys/redirect/index.vue'),
      meta: {
        title: REDIRECT_NAME,
        hideBreadcrumb: true,
      },
    },
  ],
};

export const ERROR_LOG_ROUTE: AppRouteRecordRaw = {
  path: '/error-log',
  name: 'ErrorLog',
  component: LAYOUT,
  redirect: '/error-log/list',
  meta: {
    title: 'ErrorLog',
    hideBreadcrumb: true,
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'list',
      name: 'ErrorLogList',
      component: () => import('/@/views/sys/error-log/index.vue'),
      meta: {
        title: t('routes.basic.errorLogList'),
        hideBreadcrumb: true,
        currentActiveMenu: '/error-log',
      },
    },
  ],
};

// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】路由改为DB菜单驱动，移除全屏免登录路由（避免与菜单路由冲突导致整页刷新）---
// 表单引擎菜单已迁移至 sys_permission 表（V06__formengine_menu_mysql.sql），
// 由后端菜单接口动态生成带 LAYOUT 的路由，解决全屏路由导致的整页刷新问题。
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】路由改为DB菜单驱动，移除全屏免登录路由（避免与菜单路由冲突导致整页刷新）---
