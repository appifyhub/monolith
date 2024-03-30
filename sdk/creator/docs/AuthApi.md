# appifyhub.AuthApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authenticate**](AuthApi.md#authenticate) | **POST** /v1/creator/auth | Authenticate creator
[**create_api_key**](AuthApi.md#create_api_key) | **POST** /v1/creator/apikey | Create an API key


# **authenticate**
> TokenResponse authenticate(creator_credentials_request)

Authenticate creator

Authenticates a creator using their credentials and returns a token. This token can be used for all requests that require authentication. 

### Example


```python
import appifyhub
from appifyhub.models.creator_credentials_request import CreatorCredentialsRequest
from appifyhub.models.token_response import TokenResponse
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
    api_instance = appifyhub.AuthApi(api_client)
    creator_credentials_request = appifyhub.CreatorCredentialsRequest() # CreatorCredentialsRequest | 

    try:
        # Authenticate creator
        api_response = api_instance.authenticate(creator_credentials_request)
        print("The response of AuthApi->authenticate:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->authenticate: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **creator_credentials_request** | [**CreatorCredentialsRequest**](CreatorCredentialsRequest.md)|  | 

### Return type

[**TokenResponse**](TokenResponse.md)

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

# **create_api_key**
> TokenResponse create_api_key(api_key_request)

Create an API key

Creates a long-lived API key for the creator to use for external integrations. This key can be used for all requests that require authentication. 

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.api_key_request import ApiKeyRequest
from appifyhub.models.token_response import TokenResponse
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
    api_instance = appifyhub.AuthApi(api_client)
    api_key_request = appifyhub.ApiKeyRequest() # ApiKeyRequest | 

    try:
        # Create an API key
        api_response = api_instance.create_api_key(api_key_request)
        print("The response of AuthApi->create_api_key:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->create_api_key: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **api_key_request** | [**ApiKeyRequest**](ApiKeyRequest.md)|  | 

### Return type

[**TokenResponse**](TokenResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

