# appifyhub.MessagingApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**add_template**](MessagingApi.md#add_template) | **POST** /v1/projects/{projectId}/messaging/template | Add a new message template
[**delete_template_by_id**](MessagingApi.md#delete_template_by_id) | **DELETE** /v1/projects/{projectId}/messaging/templates/{templateId} | Remove a message template
[**delete_templates**](MessagingApi.md#delete_templates) | **DELETE** /v1/projects/{projectId}/messaging/template-search | Remove message templates
[**detect_variables**](MessagingApi.md#detect_variables) | **POST** /v1/projects/{projectId}/messaging/template-variables | Detect variables in a string
[**fetch_template_by_id**](MessagingApi.md#fetch_template_by_id) | **GET** /v1/projects/{projectId}/messaging/templates/{templateId} | Get a message template
[**get_defined_variables**](MessagingApi.md#get_defined_variables) | **GET** /v1/projects/{projectId}/messaging/template-variables | Get all allowed variables
[**materialize**](MessagingApi.md#materialize) | **POST** /v1/projects/{projectId}/messaging/template-materialize | Materialize a message template (replace variables)
[**search_templates**](MessagingApi.md#search_templates) | **GET** /v1/projects/{projectId}/messaging/template-search | Search for message templates
[**update_template**](MessagingApi.md#update_template) | **PUT** /v1/projects/{projectId}/messaging/templates/{templateId} | Update a message template


# **add_template**
> MessageTemplateResponse add_template(project_id, message_template_create_request)

Add a new message template

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_template_create_request import MessageTemplateCreateRequest
from appifyhub.models.message_template_response import MessageTemplateResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    message_template_create_request = appifyhub.MessageTemplateCreateRequest() # MessageTemplateCreateRequest | 

    try:
        # Add a new message template
        api_response = api_instance.add_template(project_id, message_template_create_request)
        print("The response of MessagingApi->add_template:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->add_template: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **message_template_create_request** | [**MessageTemplateCreateRequest**](MessageTemplateCreateRequest.md)|  | 

### Return type

[**MessageTemplateResponse**](MessageTemplateResponse.md)

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

# **delete_template_by_id**
> SimpleResponse delete_template_by_id(project_id, template_id)

Remove a message template

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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    template_id = 56 # int | 

    try:
        # Remove a message template
        api_response = api_instance.delete_template_by_id(project_id, template_id)
        print("The response of MessagingApi->delete_template_by_id:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->delete_template_by_id: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **template_id** | **int**|  | 

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

# **delete_templates**
> SimpleResponse delete_templates(project_id, name=name)

Remove message templates

Search for message templates by name and language tag. When any of the query parameters are omitted, the remaining ones are used as filtering. 

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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    name = 'name_example' # str |  (optional)

    try:
        # Remove message templates
        api_response = api_instance.delete_templates(project_id, name=name)
        print("The response of MessagingApi->delete_templates:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->delete_templates: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **name** | **str**|  | [optional] 

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

# **detect_variables**
> List[VariableResponse] detect_variables(project_id, detect_variables_request)

Detect variables in a string

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.detect_variables_request import DetectVariablesRequest
from appifyhub.models.variable_response import VariableResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    detect_variables_request = appifyhub.DetectVariablesRequest() # DetectVariablesRequest | 

    try:
        # Detect variables in a string
        api_response = api_instance.detect_variables(project_id, detect_variables_request)
        print("The response of MessagingApi->detect_variables:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->detect_variables: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **detect_variables_request** | [**DetectVariablesRequest**](DetectVariablesRequest.md)|  | 

### Return type

[**List[VariableResponse]**](VariableResponse.md)

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

# **fetch_template_by_id**
> MessageTemplateResponse fetch_template_by_id(project_id, template_id)

Get a message template

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_template_response import MessageTemplateResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    template_id = 56 # int | 

    try:
        # Get a message template
        api_response = api_instance.fetch_template_by_id(project_id, template_id)
        print("The response of MessagingApi->fetch_template_by_id:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->fetch_template_by_id: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **template_id** | **int**|  | 

### Return type

[**MessageTemplateResponse**](MessageTemplateResponse.md)

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

# **get_defined_variables**
> List[VariableResponse] get_defined_variables(project_id)

Get all allowed variables

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.variable_response import VariableResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 

    try:
        # Get all allowed variables
        api_response = api_instance.get_defined_variables(project_id)
        print("The response of MessagingApi->get_defined_variables:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->get_defined_variables: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 

### Return type

[**List[VariableResponse]**](VariableResponse.md)

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

# **materialize**
> MessageResponse materialize(project_id, id=id, name=name, message_inputs_request=message_inputs_request)

Materialize a message template (replace variables)

Materializes a message template by replacing all variables with the given inputs. If the template doesn't contain any variables, then the request body is not required. A template can be located either by its ID or name. 

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_inputs_request import MessageInputsRequest
from appifyhub.models.message_response import MessageResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    id = 56 # int |  (optional)
    name = 'name_example' # str |  (optional)
    message_inputs_request = appifyhub.MessageInputsRequest() # MessageInputsRequest |  (optional)

    try:
        # Materialize a message template (replace variables)
        api_response = api_instance.materialize(project_id, id=id, name=name, message_inputs_request=message_inputs_request)
        print("The response of MessagingApi->materialize:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->materialize: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **id** | **int**|  | [optional] 
 **name** | **str**|  | [optional] 
 **message_inputs_request** | [**MessageInputsRequest**](MessageInputsRequest.md)|  | [optional] 

### Return type

[**MessageResponse**](MessageResponse.md)

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

# **search_templates**
> List[MessageTemplateResponse] search_templates(project_id, name=name, language_tag=language_tag)

Search for message templates

Search for message templates by name and language tag. When any of the query parameters are omitted, the remaining ones are used as filtering. 

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_template_response import MessageTemplateResponse
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    name = 'name_example' # str |  (optional)
    language_tag = 'language_tag_example' # str |  (optional)

    try:
        # Search for message templates
        api_response = api_instance.search_templates(project_id, name=name, language_tag=language_tag)
        print("The response of MessagingApi->search_templates:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->search_templates: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **name** | **str**|  | [optional] 
 **language_tag** | **str**|  | [optional] 

### Return type

[**List[MessageTemplateResponse]**](MessageTemplateResponse.md)

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

# **update_template**
> MessageTemplateResponse update_template(project_id, template_id, message_template_update_request)

Update a message template

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_template_response import MessageTemplateResponse
from appifyhub.models.message_template_update_request import MessageTemplateUpdateRequest
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
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    template_id = 56 # int | 
    message_template_update_request = appifyhub.MessageTemplateUpdateRequest() # MessageTemplateUpdateRequest | 

    try:
        # Update a message template
        api_response = api_instance.update_template(project_id, template_id, message_template_update_request)
        print("The response of MessagingApi->update_template:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->update_template: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **template_id** | **int**|  | 
 **message_template_update_request** | [**MessageTemplateUpdateRequest**](MessageTemplateUpdateRequest.md)|  | 

### Return type

[**MessageTemplateResponse**](MessageTemplateResponse.md)

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

