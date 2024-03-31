# FirebaseConfigDto

Firebase account configuration, primarily used for push notifications

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**project_name** | **str** | The name of the Firebase project | 
**service_account_key_json_base64** | **str** | The base64-encoded JSON representing the service account key | 

## Example

```python
from appifyhub.models.firebase_config_dto import FirebaseConfigDto

# TODO update the JSON string below
json = "{}"
# create an instance of FirebaseConfigDto from a JSON string
firebase_config_dto_instance = FirebaseConfigDto.from_json(json)
# print the JSON string representation of the object
print(FirebaseConfigDto.to_json())

# convert the object into a dict
firebase_config_dto_dict = firebase_config_dto_instance.to_dict()
# create an instance of FirebaseConfigDto from a dict
firebase_config_dto_form_dict = firebase_config_dto.from_dict(firebase_config_dto_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


