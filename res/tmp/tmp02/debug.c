class TestMain
{
    // This is the entry point of the program
    void main()
    {
        print(new Test().Compute(10));   // just a print statement
    }
}

class Test
{
    int Compute(int num)
    {
        int total;
        if ( num < 1)
        {
            total = 1;
        }
        else
        {
            total = num * (this.Compute(num-1));
        }
        return total;
    }
}