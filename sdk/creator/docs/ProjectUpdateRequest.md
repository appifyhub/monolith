# ProjectUpdateRequest

A request to update a project. Only the fields that are explicitly set will be updated. For expected data types, check the [ProjectResponse](#/components/schemas/ProjectResponse). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**status** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**name** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**description** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**logo_url** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**website_url** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**max_users** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**anyone_can_search** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**on_hold** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**language_tag** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**requires_signup_codes** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**max_signup_codes_per_user** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**mailgun_config** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**twilio_config** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**firebase_config** | [**SettableRequest**](SettableRequest.md) |  | [optional] 

## Example

```python
from appifyhub.models.project_update_request import ProjectUpdateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ProjectUpdateRequest from a JSON string
project_update_request_instance = ProjectUpdateRequest.from_json(json)
# print the JSON string representation of the object
print(ProjectUpdateRequest.to_json())

# convert the object into a dict
project_update_request_dict = project_update_request_instance.to_dict()
# create an instance of ProjectUpdateRequest from a dict
project_update_request_form_dict = project_update_request.from_dict(project_update_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


