package service.com.taoge;

public class TestService implements ITestService{

    public String queryById(String id) {
        return id + ":" + this.getClass().getSimpleName() ;
    }
}
