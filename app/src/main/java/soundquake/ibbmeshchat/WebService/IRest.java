package soundquake.ibbmeshchat.WebService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;
import soundquake.ibbmeshchat.MessageRequestObject;

/**
 * Created by Deniz on 30.05.2020.
 */
public interface IRest
{
    @POST
    Call<ResponseBody> putMessage(@Url String url, @Body MessageRequestObject body);
}
