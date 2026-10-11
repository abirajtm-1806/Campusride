-- ==============================================================
-- CampusRide Supabase Database Schema & Row Level Security (RLS) Policies
-- ==============================================================

-- 1. Table: public.students
-- Stores registered student profile details linked to Supabase Auth UUID
-- (Passwords are NEVER stored here; passwords reside strictly in auth.users)
CREATE TABLE IF NOT EXISTS public.students (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    registration_number TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT college_email_domain CHECK (email ILIKE '%@nssce.ac.in')
);

-- Enable Row Level Security (RLS) on public.students
ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;

-- Allow registered/authenticated users and client app to read student profiles
CREATE POLICY "Student profiles are viewable by all users"
ON public.students FOR SELECT
TO authenticated, anon
USING (true);

-- Allow students to update their own profile
CREATE POLICY "Students can update their own profile"
ON public.students FOR UPDATE
TO authenticated
USING (auth.uid() = id);

-- ==============================================================
-- Supabase Auth Trigger for Student Profile Creation
-- ==============================================================
-- Automatically creates the corresponding public.students profile using:
-- - auth.users.id
-- - raw_user_meta_data.full_name
-- - raw_user_meta_data.registration_number
-- - auth.users.email
-- Runs with SECURITY DEFINER so that RLS does not block automatic trigger insertion.
CREATE OR REPLACE FUNCTION public.handle_new_student()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.students (id, full_name, registration_number, email)
    VALUES (
        NEW.id,
        NEW.raw_user_meta_data->>'full_name',
        NEW.raw_user_meta_data->>'registration_number',
        NEW.email
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger: on_auth_user_created on auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_student();


-- 2. Table: public.rides
-- Stores rides offered by student drivers
CREATE TABLE IF NOT EXISTS public.rides (
    id VARCHAR(50) PRIMARY KEY,
    driver_id VARCHAR(50) NOT NULL,
    starting_location VARCHAR(200) NOT NULL,
    destination VARCHAR(200) NOT NULL,
    date VARCHAR(50) NOT NULL,
    departure_time VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL,
    available_seats INT NOT NULL CHECK (available_seats >= 0),
    cost_per_person NUMERIC(10, 2) NOT NULL CHECK (cost_per_person >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security (RLS) on public.rides
ALTER TABLE public.rides ENABLE ROW LEVEL SECURITY;

-- Allow browsing available rides
CREATE POLICY "Anyone can view available rides"
ON public.rides FOR SELECT
TO authenticated, anon
USING (true);

-- Allow authenticated students to post/offer rides
CREATE POLICY "Students can offer rides"
ON public.rides FOR INSERT
TO authenticated, anon
WITH CHECK (true);

-- Allow students to update available seats upon booking
CREATE POLICY "Students can update ride seats"
ON public.rides FOR UPDATE
TO authenticated, anon
USING (true);
