# appifyhub.ProjectsApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**add_project**](ProjectsApi.md#add_project) | **POST** /v1/projects | Creates a new project
[**get_project**](ProjectsApi.md#get_project) | **GET** /v1/projects/{projectId} | Get a project
[**get_projects**](ProjectsApi.md#get_projects) | **GET** /v1/projects | Get all projects
[**remove_project**](ProjectsApi.md#remove_project) | **DELETE** /v1/projects/{projectId} | Remove a project
[**remove_projects_by_creator**](ProjectsApi.md#remove_projects_by_creator) | **DELETE** /v1/projects | Remove all projects
[**update_project**](ProjectsApi.md#update_project) | **PUT** /v1/projects/{projectId} | Update a project


# **add_project**
> ProjectResponse add_project(project_create_request)

Creates a new project

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.project_create_request import ProjectCreateRequest
from appifyhub.models.project_response import ProjectResponse
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
    api_instance = appifyhub.ProjectsApi(api_client)
    project_create_request = appifyhub.ProjectCreateRequest() # ProjectCreateRequest | 

    try:
        # Creates a new project
        api_response = api_instance.add_project(project_create_request)
        print("The response of ProjectsApi->add_project:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->add_project: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_create_request** | [**ProjectCreateRequest**](ProjectCreateRequest.md)|  | 

### Return type

[**ProjectResponse**](ProjectResponse.md)

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

# **get_project**
> ProjectResponse get_project(project_id)

Get a project

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.project_response import ProjectResponse
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
    api_instance = appifyhub.ProjectsApi(api_client)
    project_id = 56 # int | 

    try:
        # Get a project
        api_response = api_instance.get_project(project_id)
        print("The response of ProjectsApi->get_project:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->get_project: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 

### Return type

[**ProjectResponse**](ProjectResponse.md)

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

# **get_projects**
> List[ProjectResponse] get_projects(creator_id=creator_id)

Get all projects

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.project_response import ProjectResponse
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
    api_instance = appifyhub.ProjectsApi(api_client)
    creator_id = 'creator_id_example' # str |  (optional)

    try:
        # Get all projects
        api_response = api_instance.get_projects(creator_id=creator_id)
        print("The response of ProjectsApi->get_projects:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->get_projects: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **creator_id** | **str**|  | [optional] 

### Return type

[**List[ProjectResponse]**](ProjectResponse.md)

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

# **remove_project**
> SimpleResponse remove_project(project_id)

Remove a project

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
    api_instance = appifyhub.ProjectsApi(api_client)
    project_id = 56 # int | 

    try:
        # Remove a project
        api_response = api_instance.remove_project(project_id)
        print("The response of ProjectsApi->remove_project:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->remove_project: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 

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

# **remove_projects_by_creator**
> SimpleResponse remove_projects_by_creator(creator_id)

Remove all projects

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
    api_instance = appifyhub.ProjectsApi(api_client)
    creator_id = 'creator_id_example' # str | 

    try:
        # Remove all projects
        api_response = api_instance.remove_projects_by_creator(creator_id)
        print("The response of ProjectsApi->remove_projects_by_creator:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->remove_projects_by_creator: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **creator_id** | **str**|  | 

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

# **update_project**
> ProjectResponse update_project(project_id, project_update_request)

Update a project

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.project_response import ProjectResponse
from appifyhub.models.project_update_request import ProjectUpdateRequest
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
    api_instance = appifyhub.ProjectsApi(api_client)
    project_id = 56 # int | 
    project_update_request = appifyhub.ProjectUpdateRequest() # ProjectUpdateRequest | 

    try:
        # Update a project
        api_response = api_instance.update_project(project_id, project_update_request)
        print("The response of ProjectsApi->update_project:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling ProjectsApi->update_project: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **project_update_request** | [**ProjectUpdateRequest**](ProjectUpdateRequest.md)|  | 

### Return type

[**ProjectResponse**](ProjectResponse.md)

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

