curl http://localhost:8983/solr/security_role/update -H "Content-Type: text/xml" --data-binary '
<add>
  <doc>
    <field name="id">f4251649-751e-4190-b0ed-e824f3cdd6fc</field>
    <field name="role_name_str">data_owner</field>
  </doc>
  <doc>
    <field name="id">fdeff289-daee-4ecc-8c9c-3ef111cf7a06</field>
    <field name="role_name_str">data_manager</field>
  </doc>
  <doc>
    <field name="id">47adcdaf-1f93-40ba-b8d9-9bb324eac308</field>
    <field name="role_name_str">file_viewer_editor</field>
  </doc>
</add>'

curl http://localhost:8983/solr/security_role/update --data '<commit/>' -H 'Content-type:text/xml; charset=utf-8'
