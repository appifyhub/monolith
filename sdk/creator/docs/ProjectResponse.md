# ProjectResponse

Operational details of a consumer project

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**project_id** | **int** | A unique identifier for this consumer project | 
**type** | [**ProjectType**](ProjectType.md) |  | 
**state** | [**ProjectStateResponse**](ProjectStateResponse.md) |  | 
**user_id_type** | [**ProjectUserIDType**](ProjectUserIDType.md) |  | 
**name** | **str** | The name of this consumer project | 
**description** | **str** | A short description of this consumer project | [optional] 
**logo_url** | **str** | The URL to this consumer project&#39;s logo | [optional] 
**website_url** | **str** | The URL to this consumer project&#39;s website | [optional] 
**language_tag** | **str** | The default language of the project (locale represented as in IETF BCP 47) | [optional] 
**max_users** | **int** | The maximum number of users allowed in the project | 
**anyone_can_search** | **bool** | Whether any user can search for any other user or not in the project | 
**on_hold** | **bool** | Whether the consumer project is on hold or not, preventing any actions if true | 
**requires_signup_codes** | **bool** | Whether this consumer project requires signup codes from users or not | 
**max_signup_codes_per_user** | **int** | The maximum number of signup codes that each user of this consumer project can create | 
**mailgun_config** | [**MailgunConfigDto**](MailgunConfigDto.md) |  | [optional] 
**twilio_config** | [**TwilioConfigDto**](TwilioConfigDto.md) |  | [optional] 
**firebase_config** | [**FirebaseConfigDto**](FirebaseConfigDto.md) |  | [optional] 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**updated_at** | **str** | The time the object was last updated (based on ISO 8601) | 

## Example

```python
from appifyhub.models.project_response import ProjectResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ProjectResponse from a JSON string
project_response_instance = ProjectResponse.from_json(json)
# print the JSON string representation of the object
print(ProjectResponse.to_json())

# convert the object into a dict
project_response_dict = project_response_instance.to_dict()
# create an instance of ProjectResponse from a dict
project_response_from_dict = ProjectResponse.from_dict(project_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


