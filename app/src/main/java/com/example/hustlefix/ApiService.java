package com.example.hustlefix;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface ApiService {
    // Jobs endpoints
    @GET("jobs")
    Call<List<Job>> getAllJobs();
    @GET("jobs/{id}")
    Call<Job> getJobById(@Path("id") String jobId);
    @GET("jobs/user/{userId}")
    Call<List<Job>> getJobsByUser(@Path("userId") String userId);
    @GET("jobs/category/{category}")
    Call<List<Job>> getJobsByCategory(@Path("category") String category);
    @POST("jobs")
    Call<Job> createJob(@Body Job job);
    @GET("jobs/search")
    Call<List<Job>> searchJobs(@Query("query") String query);
    // Users endpoints
    @GET("users/{id}")
    Call<User> getUserById(@Path("id") String userId);
    @POST("users/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);
    @POST("users/register")
    Call<User> registerUser(@Body User user);
    // Quotes endpoints
    @GET("quotes/job/{jobId}")
    Call<List<Quote>> getQuotesForJob(@Path("jobId") String jobId);
    @POST("quotes")
    Call<Quote> submitQuote(@Body Quote quote);
    // Messages endpoints
    @GET("messages/chat/{chatRoomId}")
    Call<List<Message>> getMessages(@Path("chatRoomId") String chatRoomId);
    @POST("messages")
    Call<Message> sendMessage(@Body Message message);
}
// Add these model classes if they don't exist yet
class LoginRequest {
    private String email;
    private String password;
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
