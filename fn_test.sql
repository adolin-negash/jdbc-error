CREATE OR REPLACE FUNCTION public.fn_test()
RETURNS table
(
	field1 int,
	field2 varchar
)
LANGUAGE plpgsql
AS $$
begin
	return query
	select
		1 as field1,
		cast('f-1' as varchar) as field2
	union all
	select
		2 as field1,
		cast('f-2' as varchar) as field2
	;
END;
$$
;