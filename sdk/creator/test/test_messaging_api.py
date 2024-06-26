# coding: utf-8

"""
    Appify Hub's Creator API

    The full specification of the service's API used by the project administrators.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from appifyhub.api.messaging_api import MessagingApi


class TestMessagingApi(unittest.TestCase):
    """MessagingApi unit test stubs"""

    def setUp(self) -> None:
        self.api = MessagingApi()

    def tearDown(self) -> None:
        pass

    def test_add_template(self) -> None:
        """Test case for add_template

        Add a new message template
        """
        pass

    def test_delete_template_by_id(self) -> None:
        """Test case for delete_template_by_id

        Remove a message template
        """
        pass

    def test_delete_templates(self) -> None:
        """Test case for delete_templates

        Remove message templates
        """
        pass

    def test_detect_variables(self) -> None:
        """Test case for detect_variables

        Detect variables in a string
        """
        pass

    def test_fetch_template_by_id(self) -> None:
        """Test case for fetch_template_by_id

        Get a message template
        """
        pass

    def test_get_defined_variables(self) -> None:
        """Test case for get_defined_variables

        Get all allowed variables
        """
        pass

    def test_materialize(self) -> None:
        """Test case for materialize

        Materialize a message template (replace variables)
        """
        pass

    def test_search_templates(self) -> None:
        """Test case for search_templates

        Search for message templates
        """
        pass

    def test_update_template(self) -> None:
        """Test case for update_template

        Update a message template
        """
        pass


if __name__ == '__main__':
    unittest.main()
