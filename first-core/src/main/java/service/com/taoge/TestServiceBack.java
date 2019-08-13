package service.com.taoge;

public class TestServiceBack implements ITestService{


    public String queryById(String id) {
        return id + ":" + this.getClass().getSimpleName() ;
    }
}
