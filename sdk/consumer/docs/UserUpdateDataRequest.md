# UserUpdateDataRequest

A request to update the user's data. Only the fields that are explicitly set will be updated. For expected data types, check the [UserSignupRequest](#/components/schemas/UserSignupRequest). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**type** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**allows_spam** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**contact** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**contact_type** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**birthday** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**company** | [**OrganizationUpdaterSettable**](OrganizationUpdaterSettable.md) |  | [optional] 
**language_tag** | [**SettableRequest**](SettableRequest.md) |  | [optional] 

## Example

```python
from appifyhub.models.user_update_data_request import UserUpdateDataRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UserUpdateDataRequest from a JSON string
user_update_data_request_instance = UserUpdateDataRequest.from_json(json)
# print the JSON string representation of the object
print(UserUpdateDataRequest.to_json())

# convert the object into a dict
user_update_data_request_dict = user_update_data_request_instance.to_dict()
# create an instance of UserUpdateDataRequest from a dict
user_update_data_request_from_dict = UserUpdateDataRequest.from_dict(user_update_data_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


