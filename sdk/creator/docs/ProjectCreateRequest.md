# ProjectCreateRequest

A request to create a new project

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | [**ProjectType**](ProjectType.md) |  | 
**user_id_type** | [**ProjectUserIDType**](ProjectUserIDType.md) |  | 
**owner_universal_id** | **str** | The universal ID of the object (creator ID and project ID separated by a dollar sign) | 
**name** | **str** | The name of this consumer project | 
**description** | **str** | A short description of this consumer project | [optional] 
**logo_url** | **str** | The URL to this consumer project&#39;s logo | [optional] 
**website_url** | **str** | The URL to this consumer project&#39;s website | [optional] 
**language_tag** | **str** | The default language of the project (locale represented as in IETF BCP 47) | [optional] 
**max_users** | **int** | The maximum number of users allowed in the project | [optional] 
**anyone_can_search** | **bool** | Whether any user can search for any other user or not in the project | [optional] 
**requires_signup_codes** | **bool** | Whether this consumer project requires signup codes from users or not | [optional] 
**max_signup_codes_per_user** | **int** | The maximum number of signup codes that each user of this consumer project can create | [optional] 
**mailgun_config** | [**MailgunConfigDto**](MailgunConfigDto.md) |  | [optional] 
**twilio_config** | [**TwilioConfigDto**](TwilioConfigDto.md) |  | [optional] 
**firebase_config** | [**FirebaseConfigDto**](FirebaseConfigDto.md) |  | [optional] 

## Example

```python
from appifyhub.models.project_create_request import ProjectCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ProjectCreateRequest from a JSON string
project_create_request_instance = ProjectCreateRequest.from_json(json)
# print the JSON string representation of the object
print(ProjectCreateRequest.to_json())

# convert the object into a dict
project_create_request_dict = project_create_request_instance.to_dict()
# create an instance of ProjectCreateRequest from a dict
project_create_request_form_dict = project_create_request.from_dict(project_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


