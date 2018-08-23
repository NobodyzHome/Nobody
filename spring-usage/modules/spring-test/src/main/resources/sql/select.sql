select * from base_dept;
select * from ext_base_line_team;
select * from base_role_line;
delete from base_dept where dept_no in (9999,8888,7777,6666,5555);
delete from ext_base_line_team where group_no in (9999,8888,7777,6666);