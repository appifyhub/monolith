# appifyhub.UsersApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**add_user**](UsersApi.md#add_user) | **POST** /v1/creator/signup | Sign up a new creator
[**force_verify_user**](UsersApi.md#force_verify_user) | **POST** /v1/universal/users/{universalId}/force-verify | Verify a user without their explicit approval


# **add_user**
> CreatorResponse add_user(creator_signup_request)

Sign up a new creator

### Example


```python
import appifyhub
from appifyhub.models.creator_response import CreatorResponse
from appifyhub.models.creator_signup_request import CreatorSignupRequest
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)


# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.UsersApi(api_client)
    creator_signup_request = appifyhub.CreatorSignupRequest() # CreatorSignupRequest | 

    try:
        # Sign up a new creator
        api_response = api_instance.add_user(creator_signup_request)
        print("The response of UsersApi->add_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UsersApi->add_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **creator_signup_request** | [**CreatorSignupRequest**](CreatorSignupRequest.md)|  | 

### Return type

[**CreatorResponse**](CreatorResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **force_verify_user**
> SimpleResponse force_verify_user(universal_id)

Verify a user without their explicit approval

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.simple_response import SimpleResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.UsersApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Verify a user without their explicit approval
        api_response = api_instance.force_verify_user(universal_id)
        print("The response of UsersApi->force_verify_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UsersApi->force_verify_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

