package top.yms.note.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteMetaExample {
    /**
     */
    protected String orderByClause;

    /**
     */
    protected boolean distinct;

    /**
     */
    protected List<Criteria> oredCriteria;

    public NoteMetaExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("f_id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("f_id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("f_id =", value, "id");
            return (Criteria) this;
        }



        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("f_id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("f_id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("f_id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("f_id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("f_id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("f_id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("f_id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("f_id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("f_id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andParentIdIsNull() {
            addCriterion("f_parent_id is null");
            return (Criteria) this;
        }

        public Criteria andParentIdIsNotNull() {
            addCriterion("f_parent_id is not null");
            return (Criteria) this;
        }

        public Criteria andParentIdEqualTo(Long value) {
            addCriterion("f_parent_id =", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotEqualTo(Long value) {
            addCriterion("f_parent_id <>", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdGreaterThan(Long value) {
            addCriterion("f_parent_id >", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdGreaterThanOrEqualTo(Long value) {
            addCriterion("f_parent_id >=", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdLessThan(Long value) {
            addCriterion("f_parent_id <", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdLessThanOrEqualTo(Long value) {
            addCriterion("f_parent_id <=", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdIn(List<Long> values) {
            addCriterion("f_parent_id in", values, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotIn(List<Long> values) {
            addCriterion("f_parent_id not in", values, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdBetween(Long value1, Long value2) {
            addCriterion("f_parent_id between", value1, value2, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotBetween(Long value1, Long value2) {
            addCriterion("f_parent_id not between", value1, value2, "parentId");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("f_user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("f_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("f_user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("f_user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("f_user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("f_user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("f_user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("f_user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("f_user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("f_user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("f_user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("f_user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("f_name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("f_name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("f_name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andEncryptedEqualTo(String value) {
            addCriterion("f_encrypted =", value, "encrypted");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("f_name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("f_name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("f_name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("f_name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("f_name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("f_name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("f_name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("f_name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("f_name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("f_name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("f_name not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andIsFileIsNull() {
            addCriterion("f_isfile is null");
            return (Criteria) this;
        }

        public Criteria andIsFileIsNotNull() {
            addCriterion("f_isfile is not null");
            return (Criteria) this;
        }

        public Criteria andIsFileEqualTo(String value) {
            addCriterion("f_isfile =", value, "isFile");
            return (Criteria) this;
        }

        public Criteria andIsFileNotEqualTo(String value) {
            addCriterion("f_isfile <>", value, "isFile");
            return (Criteria) this;
        }

        public Criteria andIsFileGreaterThan(String value) {
            addCriterion("f_isfile >", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileGreaterThanOrEqualTo(String value) {
            addCriterion("f_isfile >=", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileLessThan(String value) {
            addCriterion("f_isfile <", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileLessThanOrEqualTo(String value) {
            addCriterion("f_isfile <=", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileLike(String value) {
            addCriterion("f_isfile like", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileNotLike(String value) {
            addCriterion("f_isfile not like", value, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileIn(List<String> values) {
            addCriterion("f_isfile in", values, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileNotIn(List<String> values) {
            addCriterion("f_isfile not in", values, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileBetween(String value1, String value2) {
            addCriterion("f_isfile between", value1, value2, "IsFile");
            return (Criteria) this;
        }

        public Criteria andIsFileNotBetween(String value1, String value2) {
            addCriterion("f_isfile not between", value1, value2, "IsFile");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("f_type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("f_type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("f_type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("f_type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("f_type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("f_type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("f_type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("f_type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("f_type like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("f_type not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("f_type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("f_type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("f_type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("f_type not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andDelIsNull() {
            addCriterion("f_del is null");
            return (Criteria) this;
        }

        public Criteria andDelIsNotNull() {
            addCriterion("f_del is not null");
            return (Criteria) this;
        }

        public Criteria andDelEqualTo(String value) {
            addCriterion("f_del =", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotEqualTo(String value) {
            addCriterion("f_del <>", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelGreaterThan(String value) {
            addCriterion("f_del >", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelGreaterThanOrEqualTo(String value) {
            addCriterion("f_del >=", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelLessThan(String value) {
            addCriterion("f_del <", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelLessThanOrEqualTo(String value) {
            addCriterion("f_del <=", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelLike(String value) {
            addCriterion("f_del like", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotLike(String value) {
            addCriterion("f_del not like", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelIn(List<String> values) {
            addCriterion("f_del in", values, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotIn(List<String> values) {
            addCriterion("f_del not in", values, "del");
            return (Criteria) this;
        }

        public Criteria andDelBetween(String value1, String value2) {
            addCriterion("f_del between", value1, value2, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotBetween(String value1, String value2) {
            addCriterion("f_del not between", value1, value2, "del");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("f_create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("f_create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("f_create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("f_create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("f_create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("f_create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("f_create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("f_create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("f_create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("f_create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("f_create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("f_create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("f_update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("f_update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("f_update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("f_update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("f_update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("f_update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("f_update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("f_update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("f_update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("f_update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("f_update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("f_update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
        public Criteria andStoreSiteIsNull() {
            addCriterion("f_store_site is null");
            return (Criteria) this;
        }

        public Criteria andStoreSiteIsNotNull() {
            addCriterion("f_store_site is not null");
            return (Criteria) this;
        }

        public Criteria andStoreSiteEqualTo(String value) {
            addCriterion("f_store_site =", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteNotEqualTo(String value) {
            addCriterion("f_store_site <>", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteGreaterThan(String value) {
            addCriterion("f_store_site >", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteGreaterThanOrEqualTo(String value) {
            addCriterion("f_store_site >=", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteLessThan(String value) {
            addCriterion("f_store_site <", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteLessThanOrEqualTo(String value) {
            addCriterion("f_store_site <=", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteLike(String value) {
            addCriterion("f_store_site like", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteNotLike(String value) {
            addCriterion("f_store_site not like", value, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteIn(List<String> values) {
            addCriterion("f_store_site in", values, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteNotIn(List<String> values) {
            addCriterion("f_store_site not in", values, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteBetween(String value1, String value2) {
            addCriterion("f_store_site between", value1, value2, "storeSite");
            return (Criteria) this;
        }

        public Criteria andStoreSiteNotBetween(String value1, String value2) {
            addCriterion("f_store_site not between", value1, value2, "storeSite");
            return (Criteria) this;
        }

        public Criteria andSiteIdIsNull() {
            addCriterion("f_site_id is null");
            return (Criteria) this;
        }

        public Criteria andSiteIdIsNotNull() {
            addCriterion("f_site_id is not null");
            return (Criteria) this;
        }

        public Criteria andSiteIdEqualTo(String value) {
            addCriterion("f_site_id =", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotEqualTo(String value) {
            addCriterion("f_site_id <>", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdGreaterThan(String value) {
            addCriterion("f_site_id >", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdGreaterThanOrEqualTo(String value) {
            addCriterion("f_site_id >=", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdLessThan(String value) {
            addCriterion("f_site_id <", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdLessThanOrEqualTo(String value) {
            addCriterion("f_site_id <=", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdLike(String value) {
            addCriterion("f_site_id like", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotLike(String value) {
            addCriterion("f_site_id not like", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdIn(List<String> values) {
            addCriterion("f_site_id in", values, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotIn(List<String> values) {
            addCriterion("f_site_id not in", values, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdBetween(String value1, String value2) {
            addCriterion("f_site_id between", value1, value2, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotBetween(String value1, String value2) {
            addCriterion("f_site_id not between", value1, value2, "siteId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}