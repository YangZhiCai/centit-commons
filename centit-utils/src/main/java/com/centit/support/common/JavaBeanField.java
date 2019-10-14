package com.centit.support.common;

import com.centit.support.algorithm.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by codefan on 17-9-22.
 */
public class JavaBeanField {

    private static final Logger logger = LoggerFactory.getLogger(JavaBeanField.class);

    public JavaBeanField(){

    }

    public JavaBeanField(Field objectField){
        this.setObjectField(objectField);
    }
    // 缓存变量
    private String fieldJavaType;
    private Class<?> fieldType;
    private Method setFieldValueFunc;
    private Method getFieldValueFunc;
    private Field objectField;

    public Method getSetFieldValueFunc() {
        return setFieldValueFunc;
    }

    public void setSetFieldValueFunc(Method setFieldValueFunc) {
        this.setFieldValueFunc = setFieldValueFunc;
    }

    public Method getGetFieldValueFunc() {
        return getFieldValueFunc;
    }

    public void setGetFieldValueFunc(Method getFieldValueFunc) {
        this.getFieldValueFunc = getFieldValueFunc;
    }

    public Field getObjectField() {
        return objectField;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
        this.fieldJavaType = ReflectionOpt.getJavaTypeName(fieldType);
    }

    public void setObjectField(Field objectField) {
        this.objectField = objectField;
        this.setFieldType(objectField.getType());

    }


    public boolean isAssignableFrom(Class<?> valueType){
        return this.fieldType.isAssignableFrom(valueType);
    }


    private void innerSetObjectFieldValue(Object obj, Object fieldValue) {
        //if( fieldType.isAssignableFrom(fieldValue.getClass()) ) {
            try {
                if (setFieldValueFunc != null) {
                    setFieldValueFunc.invoke(obj, fieldValue);
                } else {

                    boolean accessible = objectField.isAccessible();
                    if (!accessible) {
                        objectField.setAccessible(true);
                    }
                    objectField.set(obj, fieldValue);
                    if (!accessible) {
                        objectField.setAccessible(accessible);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        //}
    }

    public void setObjectFieldValue(Object object,  Object newValue){
        if(newValue==null){
            //if(this.getFieldType().isPrimitive()){
            if( (this.getFieldType()!=null && this.getFieldType().isPrimitive())
                || StringUtils.equalsAny( this.fieldJavaType,
                    "int","long","float","double")){
                return;
            }
            this.innerSetObjectFieldValue(object, null);
            return;
        }

        switch (this.fieldJavaType) {
            case "int":
            case "Integer":
                this.innerSetObjectFieldValue(object,
                        NumberBaseOpt.castObjectToInteger(newValue));
                break;
            case "long":
            case "Long":
                this.innerSetObjectFieldValue(object,
                        NumberBaseOpt.castObjectToLong(newValue));
                break;
            case "float":
            case "double":
            case "Float":
            case "Double":
                this.innerSetObjectFieldValue(object,
                        NumberBaseOpt.castObjectToDouble(newValue));
                break;
            case "byte[]":
                this.innerSetObjectFieldValue(object,
                        ByteBaseOpt.castObjectToBytes(newValue));
                break;
            case "BigDecimal":
                this.innerSetObjectFieldValue(object,
                        NumberBaseOpt.castObjectToBigDecimal(newValue));
                break;
            case "BigInteger":
                this.innerSetObjectFieldValue(object,
                        NumberBaseOpt.castObjectToBigInteger(newValue));
                break;
            case "String":
                this.innerSetObjectFieldValue(object,
                        StringBaseOpt.objectToString(newValue));
                break;
            case "Date":
                this.innerSetObjectFieldValue(object,
                        DatetimeOpt.castObjectToDate(newValue));
                break;
            case "sqlDate":
                this.innerSetObjectFieldValue(object,
                        DatetimeOpt.castObjectToSqlDate(newValue));
                break;
            case "sqlTimestamp":
                this.innerSetObjectFieldValue(object,
                        DatetimeOpt.castObjectToSqlTimestamp(newValue));
                break;
            case "boolean":
            case "Boolean":
                this.innerSetObjectFieldValue(object,
                        BooleanBaseOpt.castObjectToBoolean(newValue, false));
                break;
            default:
                this.innerSetObjectFieldValue(object, newValue);
                break;
        }
    }

    public Object getObjectFieldValue(Object obj) {
        try {
            if (getFieldValueFunc != null) {
                return getFieldValueFunc.invoke(obj);
            } else {
                return objectField.get(obj);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
