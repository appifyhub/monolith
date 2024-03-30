# appifyhub.AuthApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authenticate**](AuthApi.md#authenticate) | **POST** /v1/universal/auth | Authenticate user
[**get_all_tokens**](AuthApi.md#get_all_tokens) | **GET** /v1/universal/auth/tokens | Get all tokens
[**get_current_token**](AuthApi.md#get_current_token) | **GET** /v1/universal/auth | Get the details of the current token
[**refresh**](AuthApi.md#refresh) | **PUT** /v1/universal/auth | Refresh the current token (get a new one)
[**unauthenticate**](AuthApi.md#unauthenticate) | **DELETE** /v1/universal/auth | Invalidate the current token
[**unauthenticate_tokens**](AuthApi.md#unauthenticate_tokens) | **DELETE** /v1/universal/auth/tokens | Invalidate tokens


# **authenticate**
> TokenResponse authenticate(user_credentials_request)

Authenticate user

### Example


```python
import appifyhub
from appifyhub.models.token_response import TokenResponse
from appifyhub.models.user_credentials_request import UserCredentialsRequest
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
    user_credentials_request = appifyhub.UserCredentialsRequest() # UserCredentialsRequest | 

    try:
        # Authenticate user
        api_response = api_instance.authenticate(user_credentials_request)
        print("The response of AuthApi->authenticate:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->authenticate: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **user_credentials_request** | [**UserCredentialsRequest**](UserCredentialsRequest.md)|  | 

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

# **get_all_tokens**
> List[TokenDetailsResponse] get_all_tokens(user_id=user_id, valid=valid)

Get all tokens

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.token_details_response import TokenDetailsResponse
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
    user_id = 'user_id_example' # str |  (optional)
    valid = True # bool | Whether to get only valid tokens or not (optional)

    try:
        # Get all tokens
        api_response = api_instance.get_all_tokens(user_id=user_id, valid=valid)
        print("The response of AuthApi->get_all_tokens:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->get_all_tokens: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **user_id** | **str**|  | [optional] 
 **valid** | **bool**| Whether to get only valid tokens or not | [optional] 

### Return type

[**List[TokenDetailsResponse]**](TokenDetailsResponse.md)

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

# **get_current_token**
> TokenDetailsResponse get_current_token()

Get the details of the current token

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.token_details_response import TokenDetailsResponse
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

    try:
        # Get the details of the current token
        api_response = api_instance.get_current_token()
        print("The response of AuthApi->get_current_token:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->get_current_token: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**TokenDetailsResponse**](TokenDetailsResponse.md)

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

# **refresh**
> TokenResponse refresh()

Refresh the current token (get a new one)

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
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

    try:
        # Refresh the current token (get a new one)
        api_response = api_instance.refresh()
        print("The response of AuthApi->refresh:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->refresh: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**TokenResponse**](TokenResponse.md)

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

# **unauthenticate**
> SimpleResponse unauthenticate(user_id=user_id, all=all)

Invalidate the current token

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
    api_instance = appifyhub.AuthApi(api_client)
    user_id = 'user_id_example' # str |  (optional)
    all = True # bool | Whether to unauthenticate all of the user's tokens or not (optional)

    try:
        # Invalidate the current token
        api_response = api_instance.unauthenticate(user_id=user_id, all=all)
        print("The response of AuthApi->unauthenticate:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->unauthenticate: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **user_id** | **str**|  | [optional] 
 **all** | **bool**| Whether to unauthenticate all of the user&#39;s tokens or not | [optional] 

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

# **unauthenticate_tokens**
> SimpleResponse unauthenticate_tokens(token_ids)

Invalidate tokens

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
    api_instance = appifyhub.AuthApi(api_client)
    token_ids = ['token_ids_example'] # List[str] | 

    try:
        # Invalidate tokens
        api_response = api_instance.unauthenticate_tokens(token_ids)
        print("The response of AuthApi->unauthenticate_tokens:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling AuthApi->unauthenticate_tokens: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **token_ids** | [**List[str]**](str.md)|  | 

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

