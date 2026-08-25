package com.example.hustlefix.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class PayfastRequest(
    @SerializedName("merchant_id") val merchantId: String,
    @SerializedName("merchant_key") val merchantKey: String,
    @SerializedName("return_url") val returnUrl: String,
    @SerializedName("cancel_url") val cancelUrl: String,
    @SerializedName("notify_url") val notifyUrl: String,
    @SerializedName("name_first") val firstName: String,
    @SerializedName("name_last") val lastName: String,
    @SerializedName("email_address") val email: String,
    @SerializedName("m_payment_id") val mPaymentId: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("signature") var signature: String? = null
)

data class CheckoutResponse(
    val checkoutUrl: String,
    val success: Boolean,
    val message: String?
)

interface PayfastApi {
    @POST("payments/create-checkout")
    suspend fun createCheckout(@Body request: PayfastRequest): Response<CheckoutResponse>
}

class PayfastRepository(private val api: PayfastApi) {
    suspend fun getCheckoutUrl(request: PayfastRequest): Result<String> {
        return try {
            val response = api.createCheckout(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.checkoutUrl)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get checkout URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
