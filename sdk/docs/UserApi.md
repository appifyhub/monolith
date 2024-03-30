# appifyhub.UserApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**add_user**](UserApi.md#add_user) | **POST** /v1/projects/{projectId}/signup | Sign up a new user
[**create_signup_code**](UserApi.md#create_signup_code) | **POST** /v1/universal/users/{universalId}/signup-codes | Create a signup code
[**delete_user**](UserApi.md#delete_user) | **DELETE** /v1/universal/users/{universalId} | Delete a user
[**fetch_all_signup_codes_for_user**](UserApi.md#fetch_all_signup_codes_for_user) | **GET** /v1/universal/users/{universalId}/signup-codes | Get all signup codes
[**get_user**](UserApi.md#get_user) | **GET** /v1/universal/users/{universalId} | Get a user
[**reset_signature**](UserApi.md#reset_signature) | **PUT** /v1/universal/users/{universalId}/signature/reset | Reset a user&#39;s signature
[**search_users**](UserApi.md#search_users) | **GET** /v1/projects/{projectId}/search | Search the project&#39;s users
[**update_authority**](UserApi.md#update_authority) | **PUT** /v1/universal/users/{universalId}/authority | Update a user&#39;s authority
[**update_data**](UserApi.md#update_data) | **PUT** /v1/universal/users/{universalId}/data | Update a user&#39;s data
[**update_signature**](UserApi.md#update_signature) | **PUT** /v1/universal/users/{universalId}/signature | Update a user&#39;s signature
[**verify_token**](UserApi.md#verify_token) | **PUT** /v1/universal/users/{universalId}/verify/{verificationToken} | Verify a user&#39;s signup token


# **add_user**
> UserResponse add_user(project_id, user_signup_request)

Sign up a new user

### Example


```python
import appifyhub
from appifyhub.models.user_response import UserResponse
from appifyhub.models.user_signup_request import UserSignupRequest
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
    api_instance = appifyhub.UserApi(api_client)
    project_id = 56 # int | 
    user_signup_request = appifyhub.UserSignupRequest() # UserSignupRequest | 

    try:
        # Sign up a new user
        api_response = api_instance.add_user(project_id, user_signup_request)
        print("The response of UserApi->add_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->add_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **user_signup_request** | [**UserSignupRequest**](UserSignupRequest.md)|  | 

### Return type

[**UserResponse**](UserResponse.md)

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

# **create_signup_code**
> SignupCodeResponse create_signup_code(universal_id)

Create a signup code

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.signup_code_response import SignupCodeResponse
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Create a signup code
        api_response = api_instance.create_signup_code(universal_id)
        print("The response of UserApi->create_signup_code:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->create_signup_code: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**SignupCodeResponse**](SignupCodeResponse.md)

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

# **delete_user**
> SimpleResponse delete_user(universal_id)

Delete a user

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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Delete a user
        api_response = api_instance.delete_user(universal_id)
        print("The response of UserApi->delete_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->delete_user: %s\n" % e)
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

# **fetch_all_signup_codes_for_user**
> SignupCodesResponse fetch_all_signup_codes_for_user(universal_id)

Get all signup codes

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.signup_codes_response import SignupCodesResponse
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Get all signup codes
        api_response = api_instance.fetch_all_signup_codes_for_user(universal_id)
        print("The response of UserApi->fetch_all_signup_codes_for_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->fetch_all_signup_codes_for_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**SignupCodesResponse**](SignupCodesResponse.md)

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

# **get_user**
> UserResponse get_user(universal_id)

Get a user

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.user_response import UserResponse
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Get a user
        api_response = api_instance.get_user(universal_id)
        print("The response of UserApi->get_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->get_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**UserResponse**](UserResponse.md)

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

# **reset_signature**
> SimpleResponse reset_signature(universal_id)

Reset a user's signature

This will reset the user's signature to a new one. The new signature will be sent to the user's contact channel. 

### Example


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


# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Reset a user's signature
        api_response = api_instance.reset_signature(universal_id)
        print("The response of UserApi->reset_signature:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->reset_signature: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **search_users**
> List[UserResponse] search_users(project_id, user_name=user_name, user_contact=user_contact)

Search the project's users

Search the users of the project by their name or contact information. At least one of the search parameters is required to perform a search. 

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.user_response import UserResponse
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
    api_instance = appifyhub.UserApi(api_client)
    project_id = 56 # int | 
    user_name = 'user_name_example' # str |  (optional)
    user_contact = 'user_contact_example' # str |  (optional)

    try:
        # Search the project's users
        api_response = api_instance.search_users(project_id, user_name=user_name, user_contact=user_contact)
        print("The response of UserApi->search_users:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->search_users: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **user_name** | **str**|  | [optional] 
 **user_contact** | **str**|  | [optional] 

### Return type

[**List[UserResponse]**](UserResponse.md)

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

# **update_authority**
> UserResponse update_authority(universal_id, user_update_authority_request)

Update a user's authority

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.user_response import UserResponse
from appifyhub.models.user_update_authority_request import UserUpdateAuthorityRequest
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 
    user_update_authority_request = appifyhub.UserUpdateAuthorityRequest() # UserUpdateAuthorityRequest | 

    try:
        # Update a user's authority
        api_response = api_instance.update_authority(universal_id, user_update_authority_request)
        print("The response of UserApi->update_authority:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->update_authority: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **user_update_authority_request** | [**UserUpdateAuthorityRequest**](UserUpdateAuthorityRequest.md)|  | 

### Return type

[**UserResponse**](UserResponse.md)

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

# **update_data**
> UserResponse update_data(universal_id, user_update_data_request)

Update a user's data

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.user_response import UserResponse
from appifyhub.models.user_update_data_request import UserUpdateDataRequest
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 
    user_update_data_request = appifyhub.UserUpdateDataRequest() # UserUpdateDataRequest | 

    try:
        # Update a user's data
        api_response = api_instance.update_data(universal_id, user_update_data_request)
        print("The response of UserApi->update_data:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->update_data: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **user_update_data_request** | [**UserUpdateDataRequest**](UserUpdateDataRequest.md)|  | 

### Return type

[**UserResponse**](UserResponse.md)

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

# **update_signature**
> UserResponse update_signature(universal_id, user_update_signature_request)

Update a user's signature

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.user_response import UserResponse
from appifyhub.models.user_update_signature_request import UserUpdateSignatureRequest
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
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 
    user_update_signature_request = appifyhub.UserUpdateSignatureRequest() # UserUpdateSignatureRequest | 

    try:
        # Update a user's signature
        api_response = api_instance.update_signature(universal_id, user_update_signature_request)
        print("The response of UserApi->update_signature:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->update_signature: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **user_update_signature_request** | [**UserUpdateSignatureRequest**](UserUpdateSignatureRequest.md)|  | 

### Return type

[**UserResponse**](UserResponse.md)

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

# **verify_token**
> SimpleResponse verify_token(universal_id, verification_token)

Verify a user's signup token

### Example


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


# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.UserApi(api_client)
    universal_id = 'universal_id_example' # str | 
    verification_token = 'verification_token_example' # str | 

    try:
        # Verify a user's signup token
        api_response = api_instance.verify_token(universal_id, verification_token)
        print("The response of UserApi->verify_token:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling UserApi->verify_token: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **verification_token** | **str**|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

