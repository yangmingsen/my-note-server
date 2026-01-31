package top.yms.note.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileStoreRelationExample {
    /**
     */
    protected String orderByClause;

    /**
     */
    protected boolean distinct;

    /**
     */
    protected List<Criteria> oredCriteria;

    public FileStoreRelationExample() {
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

        public Criteria andMongoFileIdIsNull() {
            addCriterion("f_mongo_file_id is null");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdIsNotNull() {
            addCriterion("f_mongo_file_id is not null");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdEqualTo(String value) {
            addCriterion("f_mongo_file_id =", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdNotEqualTo(String value) {
            addCriterion("f_mongo_file_id <>", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdGreaterThan(String value) {
            addCriterion("f_mongo_file_id >", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdGreaterThanOrEqualTo(String value) {
            addCriterion("f_mongo_file_id >=", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdLessThan(String value) {
            addCriterion("f_mongo_file_id <", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdLessThanOrEqualTo(String value) {
            addCriterion("f_mongo_file_id <=", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdLike(String value) {
            addCriterion("f_mongo_file_id like", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdNotLike(String value) {
            addCriterion("f_mongo_file_id not like", value, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdIn(List<String> values) {
            addCriterion("f_mongo_file_id in", values, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdNotIn(List<String> values) {
            addCriterion("f_mongo_file_id not in", values, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdBetween(String value1, String value2) {
            addCriterion("f_mongo_file_id between", value1, value2, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andMongoFileIdNotBetween(String value1, String value2) {
            addCriterion("f_mongo_file_id not between", value1, value2, "mongoFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdIsNull() {
            addCriterion("f_storage_file_id is null");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdIsNotNull() {
            addCriterion("f_storage_file_id is not null");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdEqualTo(String value) {
            addCriterion("f_storage_file_id =", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdNotEqualTo(String value) {
            addCriterion("f_storage_file_id <>", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdGreaterThan(String value) {
            addCriterion("f_storage_file_id >", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdGreaterThanOrEqualTo(String value) {
            addCriterion("f_storage_file_id >=", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdLessThan(String value) {
            addCriterion("f_storage_file_id <", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdLessThanOrEqualTo(String value) {
            addCriterion("f_storage_file_id <=", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdLike(String value) {
            addCriterion("f_storage_file_id like", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdNotLike(String value) {
            addCriterion("f_storage_file_id not like", value, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdIn(List<String> values) {
            addCriterion("f_storage_file_id in", values, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdNotIn(List<String> values) {
            addCriterion("f_storage_file_id not in", values, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdBetween(String value1, String value2) {
            addCriterion("f_storage_file_id between", value1, value2, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andStorageFileIdNotBetween(String value1, String value2) {
            addCriterion("f_storage_file_id not between", value1, value2, "storageFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdIsNull() {
            addCriterion("f_note_file_id is null");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdIsNotNull() {
            addCriterion("f_note_file_id is not null");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdEqualTo(String value) {
            addCriterion("f_note_file_id =", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdNotEqualTo(String value) {
            addCriterion("f_note_file_id <>", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdGreaterThan(String value) {
            addCriterion("f_note_file_id >", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdGreaterThanOrEqualTo(String value) {
            addCriterion("f_note_file_id >=", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdLessThan(String value) {
            addCriterion("f_note_file_id <", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdLessThanOrEqualTo(String value) {
            addCriterion("f_note_file_id <=", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdLike(String value) {
            addCriterion("f_note_file_id like", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdNotLike(String value) {
            addCriterion("f_note_file_id not like", value, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdIn(List<String> values) {
            addCriterion("f_note_file_id in", values, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdNotIn(List<String> values) {
            addCriterion("f_note_file_id not in", values, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdBetween(String value1, String value2) {
            addCriterion("f_note_file_id between", value1, value2, "noteFileId");
            return (Criteria) this;
        }

        public Criteria andNoteFileIdNotBetween(String value1, String value2) {
            addCriterion("f_note_file_id not between", value1, value2, "noteFileId");
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