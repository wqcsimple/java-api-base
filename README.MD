## JAVA Api Base
> java Api 基础库，会把一些常用的方法和最佳实例进行封装



## 20180608 

> add pagination helper: val (offset, length) = Pagination.config(page) or Pagination.config(page, 20) 


## 生成uid方法

```java
String uid = uidService.get(new UIDRunner() {
    @Override
    public String generate() {
        return RandomStringUtils.randomAlphanumeric(9);
    }
    
    @Override
    public Boolean exists(String uid) {
        return userMapper.getUserByUid(uid) != null;
    }
    
    @Override
    public String lockKey(String uid) {
        return Const.getKeyOfUserUidLock(uid);
    }
});

```