create table base_dept(
	dept_no integer primary key identity,
	dept_code varchar(20),
	dept_name varchar(20),
	dept_level integer,
	parent_dept_no integer,
	modify_date datetime,
	is_run integer
);