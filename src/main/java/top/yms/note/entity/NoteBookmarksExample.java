package top.yms.note.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteBookmarksExample {
    /**
     */
    protected String orderByClause;

    /**
     */
    protected boolean distinct;

    /**
     */
    protected List<Criteria> oredCriteria;

    public NoteBookmarksExample() {
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

        public Criteria andUrlIsNull() {
            addCriterion("f_url is null");
            return (Criteria) this;
        }

        public Criteria andUrlIsNotNull() {
            addCriterion("f_url is not null");
            return (Criteria) this;
        }

        public Criteria andUrlEqualTo(String value) {
            addCriterion("f_url =", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotEqualTo(String value) {
            addCriterion("f_url <>", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThan(String value) {
            addCriterion("f_url >", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThanOrEqualTo(String value) {
            addCriterion("f_url >=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThan(String value) {
            addCriterion("f_url <", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThanOrEqualTo(String value) {
            addCriterion("f_url <=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLike(String value) {
            addCriterion("f_url like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotLike(String value) {
            addCriterion("f_url not like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlIn(List<String> values) {
            addCriterion("f_url in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotIn(List<String> values) {
            addCriterion("f_url not in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlBetween(String value1, String value2) {
            addCriterion("f_url between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotBetween(String value1, String value2) {
            addCriterion("f_url not between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andChromeIdIsNull() {
            addCriterion("f_chrome_id is null");
            return (Criteria) this;
        }

        public Criteria andChromeIdIsNotNull() {
            addCriterion("f_chrome_id is not null");
            return (Criteria) this;
        }

        public Criteria andChromeIdEqualTo(Long value) {
            addCriterion("f_chrome_id =", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdNotEqualTo(Long value) {
            addCriterion("f_chrome_id <>", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdGreaterThan(Long value) {
            addCriterion("f_chrome_id >", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("f_chrome_id >=", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdLessThan(Long value) {
            addCriterion("f_chrome_id <", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdLessThanOrEqualTo(Long value) {
            addCriterion("f_chrome_id <=", value, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdIn(List<Long> values) {
            addCriterion("f_chrome_id in", values, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdNotIn(List<Long> values) {
            addCriterion("f_chrome_id not in", values, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdBetween(Long value1, Long value2) {
            addCriterion("f_chrome_id between", value1, value2, "chromeId");
            return (Criteria) this;
        }

        public Criteria andChromeIdNotBetween(Long value1, Long value2) {
            addCriterion("f_chrome_id not between", value1, value2, "chromeId");
            return (Criteria) this;
        }

        public Criteria andGuidIsNull() {
            addCriterion("f_guid is null");
            return (Criteria) this;
        }

        public Criteria andGuidIsNotNull() {
            addCriterion("f_guid is not null");
            return (Criteria) this;
        }

        public Criteria andGuidEqualTo(String value) {
            addCriterion("f_guid =", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidNotEqualTo(String value) {
            addCriterion("f_guid <>", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidGreaterThan(String value) {
            addCriterion("f_guid >", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidGreaterThanOrEqualTo(String value) {
            addCriterion("f_guid >=", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidLessThan(String value) {
            addCriterion("f_guid <", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidLessThanOrEqualTo(String value) {
            addCriterion("f_guid <=", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidLike(String value) {
            addCriterion("f_guid like", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidNotLike(String value) {
            addCriterion("f_guid not like", value, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidIn(List<String> values) {
            addCriterion("f_guid in", values, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidNotIn(List<String> values) {
            addCriterion("f_guid not in", values, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidBetween(String value1, String value2) {
            addCriterion("f_guid between", value1, value2, "guid");
            return (Criteria) this;
        }

        public Criteria andGuidNotBetween(String value1, String value2) {
            addCriterion("f_guid not between", value1, value2, "guid");
            return (Criteria) this;
        }

        public Criteria andDateAddedIsNull() {
            addCriterion("f_date_added is null");
            return (Criteria) this;
        }

        public Criteria andDateAddedIsNotNull() {
            addCriterion("f_date_added is not null");
            return (Criteria) this;
        }

        public Criteria andDateAddedEqualTo(String value) {
            addCriterion("f_date_added =", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedNotEqualTo(String value) {
            addCriterion("f_date_added <>", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedGreaterThan(String value) {
            addCriterion("f_date_added >", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedGreaterThanOrEqualTo(String value) {
            addCriterion("f_date_added >=", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedLessThan(String value) {
            addCriterion("f_date_added <", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedLessThanOrEqualTo(String value) {
            addCriterion("f_date_added <=", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedLike(String value) {
            addCriterion("f_date_added like", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedNotLike(String value) {
            addCriterion("f_date_added not like", value, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedIn(List<String> values) {
            addCriterion("f_date_added in", values, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedNotIn(List<String> values) {
            addCriterion("f_date_added not in", values, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedBetween(String value1, String value2) {
            addCriterion("f_date_added between", value1, value2, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateAddedNotBetween(String value1, String value2) {
            addCriterion("f_date_added not between", value1, value2, "dateAdded");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedIsNull() {
            addCriterion("f_date_last_used is null");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedIsNotNull() {
            addCriterion("f_date_last_used is not null");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedEqualTo(String value) {
            addCriterion("f_date_last_used =", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedNotEqualTo(String value) {
            addCriterion("f_date_last_used <>", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedGreaterThan(String value) {
            addCriterion("f_date_last_used >", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedGreaterThanOrEqualTo(String value) {
            addCriterion("f_date_last_used >=", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedLessThan(String value) {
            addCriterion("f_date_last_used <", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedLessThanOrEqualTo(String value) {
            addCriterion("f_date_last_used <=", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedLike(String value) {
            addCriterion("f_date_last_used like", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedNotLike(String value) {
            addCriterion("f_date_last_used not like", value, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedIn(List<String> values) {
            addCriterion("f_date_last_used in", values, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedNotIn(List<String> values) {
            addCriterion("f_date_last_used not in", values, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedBetween(String value1, String value2) {
            addCriterion("f_date_last_used between", value1, value2, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andDateLastUsedNotBetween(String value1, String value2) {
            addCriterion("f_date_last_used not between", value1, value2, "dateLastUsed");
            return (Criteria) this;
        }

        public Criteria andMetaInfoIsNull() {
            addCriterion("f_meta_info is null");
            return (Criteria) this;
        }

        public Criteria andMetaInfoIsNotNull() {
            addCriterion("f_meta_info is not null");
            return (Criteria) this;
        }

        public Criteria andMetaInfoEqualTo(String value) {
            addCriterion("f_meta_info =", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoNotEqualTo(String value) {
            addCriterion("f_meta_info <>", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoGreaterThan(String value) {
            addCriterion("f_meta_info >", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoGreaterThanOrEqualTo(String value) {
            addCriterion("f_meta_info >=", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoLessThan(String value) {
            addCriterion("f_meta_info <", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoLessThanOrEqualTo(String value) {
            addCriterion("f_meta_info <=", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoLike(String value) {
            addCriterion("f_meta_info like", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoNotLike(String value) {
            addCriterion("f_meta_info not like", value, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoIn(List<String> values) {
            addCriterion("f_meta_info in", values, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoNotIn(List<String> values) {
            addCriterion("f_meta_info not in", values, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoBetween(String value1, String value2) {
            addCriterion("f_meta_info between", value1, value2, "metaInfo");
            return (Criteria) this;
        }

        public Criteria andMetaInfoNotBetween(String value1, String value2) {
            addCriterion("f_meta_info not between", value1, value2, "metaInfo");
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

        public Criteria andSyncFlagIsNull() {
            addCriterion("f_sync_flag is null");
            return (Criteria) this;
        }

        public Criteria andSyncFlagIsNotNull() {
            addCriterion("f_sync_flag is not null");
            return (Criteria) this;
        }

        public Criteria andSyncFlagEqualTo(String value) {
            addCriterion("f_sync_flag =", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagNotEqualTo(String value) {
            addCriterion("f_sync_flag <>", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagGreaterThan(String value) {
            addCriterion("f_sync_flag >", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagGreaterThanOrEqualTo(String value) {
            addCriterion("f_sync_flag >=", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagLessThan(String value) {
            addCriterion("f_sync_flag <", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagLessThanOrEqualTo(String value) {
            addCriterion("f_sync_flag <=", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagLike(String value) {
            addCriterion("f_sync_flag like", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagNotLike(String value) {
            addCriterion("f_sync_flag not like", value, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagIn(List<String> values) {
            addCriterion("f_sync_flag in", values, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagNotIn(List<String> values) {
            addCriterion("f_sync_flag not in", values, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagBetween(String value1, String value2) {
            addCriterion("f_sync_flag between", value1, value2, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncFlagNotBetween(String value1, String value2) {
            addCriterion("f_sync_flag not between", value1, value2, "syncFlag");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeIsNull() {
            addCriterion("f_sync_last_time is null");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeIsNotNull() {
            addCriterion("f_sync_last_time is not null");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeEqualTo(Date value) {
            addCriterion("f_sync_last_time =", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeNotEqualTo(Date value) {
            addCriterion("f_sync_last_time <>", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeGreaterThan(Date value) {
            addCriterion("f_sync_last_time >", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("f_sync_last_time >=", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeLessThan(Date value) {
            addCriterion("f_sync_last_time <", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeLessThanOrEqualTo(Date value) {
            addCriterion("f_sync_last_time <=", value, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeIn(List<Date> values) {
            addCriterion("f_sync_last_time in", values, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeNotIn(List<Date> values) {
            addCriterion("f_sync_last_time not in", values, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeBetween(Date value1, Date value2) {
            addCriterion("f_sync_last_time between", value1, value2, "syncLastTime");
            return (Criteria) this;
        }

        public Criteria andSyncLastTimeNotBetween(Date value1, Date value2) {
            addCriterion("f_sync_last_time not between", value1, value2, "syncLastTime");
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